package com.example.projetinhoArquivo.service;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;

@Service
public class ProtocoloService {

    private static final String CAMINHO_BASE =
        "Y:/UNIDADE ADMINISTRATIVA/TI/PROTESTOS_PROTOCOLOS";

    public byte[] gerarDocumento(String cooperado,
                                String cpfCnpj,
                                String protocolo,
                                String livro,
                                String folha) throws IOException {

        ClassPathResource resource =
                new ClassPathResource("templates-doc/modelo_protocolo.docx");

        InputStream inputStream = resource.getInputStream();
        XWPFDocument document = new XWPFDocument(inputStream);

        substituirTexto(document, "${cooperado}", cooperado);
        substituirTexto(document, "${cpfCnpj}", cpfCnpj);
        substituirTexto(document, "${protocolo}", protocolo);
        substituirTexto(document, "${livro}", livro);
        substituirTexto(document, "${folha}", folha);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        document.close();

        byte[] arquivoBytes = out.toByteArray();

        // 🔥 Nome limpo para pasta
        String nomeLimpo = limparNomeArquivo(cooperado);

        // 🔥 Criando pasta do cooperado
        Path pastaCooperado = Paths.get(CAMINHO_BASE, nomeLimpo);

        if (!Files.exists(pastaCooperado)) {
            Files.createDirectories(pastaCooperado);
        }

        // 🔥 Nome final do arquivo
        String nomeArquivo =
                "Protocolo_" + nomeLimpo + "_" + protocolo + ".docx";

        Path caminhoFinal = pastaCooperado.resolve(nomeArquivo);

        Files.write(caminhoFinal,
                arquivoBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        return arquivoBytes;
    }

    private String limparNomeArquivo(String nome) {

        String normalizado = Normalizer.normalize(nome,
                Normalizer.Form.NFD);

        normalizado = normalizado.replaceAll("[^\\p{ASCII}]", "");
        normalizado = normalizado.replaceAll("[\\\\/:*?\"<>|]", "");
        normalizado = normalizado.trim().replaceAll("\\s+", "_");

        return normalizado.toUpperCase(); // 🔥 opcional: tudo maiúsculo
    }

    private void substituirTexto(XWPFDocument document,
                                 String chave,
                                 String valor) {

        for (XWPFParagraph paragraph : document.getParagraphs()) {

            String textoCompleto = paragraph.getText();

            if (textoCompleto.contains(chave)) {

                textoCompleto = textoCompleto.replace(chave, valor);

                int runs = paragraph.getRuns().size();
                for (int i = runs - 1; i >= 0; i--) {
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