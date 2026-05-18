package com.example.projetinhoArquivo.controller;

import com.example.projetinhoArquivo.config.AppProperties;
import com.example.projetinhoArquivo.dto.ProtestoDTO;
import com.example.projetinhoArquivo.service.FileService;
import com.example.projetinhoArquivo.service.ProtocoloService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ProtocoloService protocoloService;

    // ===============================
    // HOME
    // ===============================
    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("origem", appProperties.getOrigem());
        model.addAttribute("destino", appProperties.getDestino());

        return "index";
    }

    // ===============================
    // ORGANIZADOR DE ARQUIVOS
    // ===============================
    @PostMapping("/executar")
    public String executar(
            @RequestParam String origem,
            @RequestParam String destino,
            Model model) {

        String resultado = fileService.executarProcesso(origem, destino);

        model.addAttribute("resultado", resultado);
        model.addAttribute("origem", origem);
        model.addAttribute("destino", destino);

        return "index";
    }

    // ===============================
    // GERAR PROTOCOLO (WORD ou PDF)
    // ===============================
        @PostMapping("/gerar-protocolo")
        // public ResponseEntity<byte[]> gerarProtocolo(
        public Object gerarProtocolo(
                Model model,
                @RequestParam String cooperado,
                @RequestParam String cpfCnpj,
                @RequestParam int quantidade,
                @RequestParam String tipoArquivo,
                HttpServletRequest request) throws Exception {

        List<ProtestoDTO> protestos = new ArrayList<>();
        StringBuilder protocolosNome = new StringBuilder();

        for (int i = 1; i <= quantidade; i++) {

                String protocolo = request.getParameter("protocolo" + i);
                String livro = request.getParameter("livro" + i);
                String folha = request.getParameter("folha" + i);

                protestos.add(new ProtestoDTO(protocolo, livro, folha));

                protocolosNome.append(protocolo);

                if (i < quantidade) {
                protocolosNome.append("-");
                }
        }

        byte[] arquivo = protocoloService.gerarDocumento(
                cooperado,
                cpfCnpj,
                protestos,
                tipoArquivo
        );

        // 🔥 LIMPEZA DO NOME
        String nomeLimpo = cooperado
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[\\\\/:*?\"<>|]", "")
                .trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();

        String extensao;
        String contentType;

        if (tipoArquivo.equalsIgnoreCase("pdf")) {
                extensao = ".pdf";
                contentType = "application/pdf";
        } else {
                extensao = ".docx";
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        String nomeArquivo =
                "Protocolo_" + nomeLimpo + "_" + protocolosNome + extensao;

        return ResponseEntity.ok()
        .header("Content-Disposition",
                tipoArquivo.equalsIgnoreCase("pdf")
                        ? "inline; filename=" + nomeArquivo
                        : "attachment; filename=" + nomeArquivo)
        .header("Content-Type", contentType)
        .body(arquivo);
        }    

}