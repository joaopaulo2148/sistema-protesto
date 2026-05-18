package com.example.projetinhoArquivo.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.projetinhoArquivo.config.AppProperties;
import com.example.projetinhoArquivo.service.FileService;
import com.example.projetinhoArquivo.service.ProtocoloService;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    //  Pegando valores do properties
    // @Value("${app.caminho.origem}")
    // private String caminhoPadraoOrigem;

    // @Value("${app.caminho.destino}")
    // private String caminhoPadraoDestino;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private ProtocoloService protocoloService;

    @GetMapping("/")
    public String home(Model model) {

        // Envia os caminhos padrão para a tela
        model.addAttribute("origem", appProperties.getOrigem());
        model.addAttribute("destino", appProperties.getDestino());

        return "index";
    }

    @PostMapping("/executar")
    public String executar(
            @RequestParam String origem,
            @RequestParam String destino,
            Model model) {

        String resultado = fileService.executarProcesso(origem, destino);

        model.addAttribute("resultado", resultado);

        // 🔁 Mantém os valores digitados após execução
        model.addAttribute("origem", origem);
        model.addAttribute("destino", destino);

        return "index";
    }

   @PostMapping("/gerar-protocolo")
        public ResponseEntity<byte[]> gerarProtocolo(
                @RequestParam String cooperado,
                @RequestParam String cpfCnpj,
                @RequestParam String protocolo,
                @RequestParam String livro,
                @RequestParam String folha) throws IOException {

        byte[] documento = protocoloService.gerarDocumento(
                cooperado, cpfCnpj, protocolo, livro, folha);

        String nomeArquivo = "Protocolo_" 
                + cooperado.replaceAll("\\s+", "_")
                + "_" + protocolo + ".docx";

        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=" + nomeArquivo)
                .body(documento);
        }
}
