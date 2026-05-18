package com.example.projetinhoArquivo.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.projetinhoArquivo.dto.ProtestoDTO;

import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.List;

@Service
public class ProtocoloService {

    private static final String CAMINHO_BASE =
        "Y:/UNIDADE ADMINISTRATIVA/TI/PROTESTOS_PROTOCOLOS";

    public byte[] gerarDocumento(String cooperado,
                                 String cpfCnpj,
                                 List<ProtestoDTO> protestos,
                                 String tipoArquivo) throws Exception {

        // 🔹 Carrega modelo
        ClassPathResource resource =
                new ClassPathResource("documents/modelo_protocolo.docx");

        XWPFDocument document =
                new XWPFDocument(resource.getInputStream());

        substituirTexto(document, "${cooperado}", cooperado);
        substituirTexto(document, "${cpfCnpj}", cpfCnpj);

        inserirProtestos(document, protestos);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        byte[] docxBytes = out.toByteArray();

        // 🔹 Limpar nome
        String nomeLimpo = limparNomeArquivo(cooperado);

        // 🔹 Criar pasta do cooperado
        Path pastaCooperado = Paths.get(CAMINHO_BASE, nomeLimpo);
        if (!Files.exists(pastaCooperado)) {
            Files.createDirectories(pastaCooperado);
        }

        // 🔹 Montar nome com múltiplos protocolos
        String protocolosNome = protestos.stream()
                .map(ProtestoDTO::getProtocolo)
                .reduce((p1, p2) -> p1 + "-" + p2)
                .orElse("SEM_PROTOCOLO");

        String nomeBase =
                "Protocolo_" + nomeLimpo + "_" + protocolosNome;

        Path caminhoDocx = pastaCooperado.resolve(nomeBase + ".docx");

        Files.write(caminhoDocx,
                docxBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        // 🔥 Se for PDF → converte
        //-----ATENÇÃO------
        //Necessário ter instalado o LibreOffice para conversão de arquivos PDF
        if ("pdf".equalsIgnoreCase(tipoArquivo)) {

            ProcessBuilder pb = new ProcessBuilder(
                    "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                    "--headless",
                    "--convert-to", "pdf",
                    "--outdir", pastaCooperado.toString(),
                    caminhoDocx.toString()
            );

            pb.start().waitFor();

            Path caminhoPdf = pastaCooperado.resolve(nomeBase + ".pdf");

            byte[] pdfBytes = Files.readAllBytes(caminhoPdf);

            return pdfBytes;
        }

        return docxBytes;
    }

    // 🔥 Insere protestos com quebra REAL no Word
    private void inserirProtestos(XWPFDocument document,
                                  List<ProtestoDTO> protestos) {

        for (XWPFParagraph paragraph : document.getParagraphs()) {

            if (paragraph.getText().contains("${linhaProtesto}")) {

                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }

                for (int i = 0; i < protestos.size(); i++) {

                    ProtestoDTO p = protestos.get(i);

                    XWPFRun run = paragraph.createRun();
                    run.setFontFamily("Calibri");
                    run.setFontSize(12);

                    run.setText("Protocolo: " + p.getProtocolo()
                            + " - Livro: " + p.getLivro()
                            + " - Folha: " + p.getFolha());

                    if (i < protestos.size() - 1) {
                        run.addBreak(); // 🔥 quebra real
                    }
                }
            }
        }
    }

    private String limparNomeArquivo(String nome) {

        String normalizado = Normalizer.normalize(nome,
                Normalizer.Form.NFD);

        normalizado = normalizado.replaceAll("[^\\p{ASCII}]", "");
        normalizado = normalizado.replaceAll("[\\\\/:*?\"<>|]", "");
        normalizado = normalizado.trim().replaceAll("\\s+", "_");

        return normalizado.toUpperCase();
    }

    private void substituirTexto(XWPFDocument document,
                                 String chave,
                                 String valor) {

        for (XWPFParagraph paragraph : document.getParagraphs()) {

            if (paragraph.getText().contains(chave)) {

                String textoCompleto =
                        paragraph.getText().replace(chave, valor);

                for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }

                XWPFRun novoRun = paragraph.createRun();
                novoRun.setText(textoCompleto);
                novoRun.setFontFamily("Calibri");
                novoRun.setFontSize(12);
            }
        }
    }
}