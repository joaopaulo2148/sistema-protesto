package com.example.projetinhoArquivo.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Service
public class FileService {

    public String executarProcesso(String caminhoOrigem, String caminhoDestino) {

        StringBuilder log = new StringBuilder();

        File pastaOrigem = new File(caminhoOrigem);
        File pastaDestino = new File(caminhoDestino);

        if (!pastaOrigem.exists() || !pastaOrigem.isDirectory()) {
            return "A pasta de origem não existe ou não é um diretório.";
        }

        if (!pastaDestino.exists()) {
            pastaDestino.mkdirs();
            log.append("Pasta de destino criada: ")
               .append(pastaDestino.getAbsolutePath()).append("\n");
        }

        moverArquivos(pastaOrigem, pastaDestino, log);
        organizarPorBeneficiario(pastaDestino, log);

        log.append("\n✅ Processo finalizado com sucesso!");
        return log.toString();
    }

    private void moverArquivos(File origem, File destino, StringBuilder log) {

    File[] arquivos = origem.listFiles(File::isFile);

    if (arquivos == null || arquivos.length == 0) {
        log.append("Nenhum arquivo encontrado na pasta de origem.\n");
        return;
    }

    for (File arquivo : arquivos) {

        String nomeArquivo = arquivo.getName();

        // 🔎 NOVA REGRA: só move se tiver "="
        if (!nomeArquivo.contains("=")) {
            log.append("❌ Arquivo ignorado (não contém '='): ")
               .append(nomeArquivo)
               .append("\n");
            continue; // pula para o próximo arquivo
        }

        Path origemPath = arquivo.toPath();
        Path destinoPath = Paths.get(destino.getAbsolutePath(), nomeArquivo);

        try {
            Files.move(origemPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
            log.append("✅ Arquivo movido: ")
               .append(nomeArquivo)
               .append("\n");

        } catch (IOException e) {
            log.append("Erro ao mover ")
               .append(nomeArquivo)
               .append(": ")
               .append(e.getMessage())
               .append("\n");
        }
    }
}

    private void organizarPorBeneficiario(File pastaDestino, StringBuilder log) {

        File[] arquivos = pastaDestino.listFiles(File::isFile);

        if (arquivos == null || arquivos.length == 0) {
            log.append("Nenhum arquivo encontrado na pasta de destino.\n");
            return;
        }

        for (File arquivo : arquivos) {

            String nomeArquivo = arquivo.getName();

            if (nomeArquivo.contains("=")) {

                String beneficiario = nomeArquivo.substring(nomeArquivo.indexOf("=") + 1);

                if (beneficiario.contains(".")) {
                    beneficiario = beneficiario.substring(0, beneficiario.lastIndexOf("."));
                }

                File pastaBeneficiario = new File(pastaDestino, beneficiario);

                if (!pastaBeneficiario.exists()) {
                    pastaBeneficiario.mkdirs();
                    log.append("📁 Pasta criada: ")
                       .append(pastaBeneficiario.getAbsolutePath()).append("\n");
                }

                Path origem = arquivo.toPath();
                Path destino = Paths.get(pastaBeneficiario.getAbsolutePath(), arquivo.getName());

                try {
                    Files.move(origem, destino, StandardCopyOption.REPLACE_EXISTING);
                    log.append("➡️ Arquivo movido: ")
                       .append(nomeArquivo)
                       .append(" → ")
                       .append(beneficiario)
                       .append("\n");
                } catch (IOException e) {
                    log.append("Erro ao mover ")
                       .append(nomeArquivo)
                       .append(": ")
                       .append(e.getMessage())
                       .append("\n");
                }

            } else {
                log.append("Arquivo ignorado (não contém '='): ")
                   .append(nomeArquivo).append("\n");
            }
        }
    }
}
