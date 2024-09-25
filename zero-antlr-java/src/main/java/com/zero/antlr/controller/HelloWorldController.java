package com.zero.antlr.controller;

import com.zero.antlr.service.impl.EvalListener;
import com.zero.antlr.service.impl.EvalVisitor;

//注意：这两个类是 运行 mvn antlr4:antlr4 命令后自动生成的
import com.zero.antlr.common.generator.LabeledExprLexer;
import com.zero.antlr.common.generator.LabeledExprParser;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class HelloWorldController {

    @RequestMapping("/hello")
    public Map<String, Object> showHelloWorld() {
        Map<String, Object> map = new HashMap<>();
        map.put("msg", "HelloWorld");
        return map;
    }

    @SneakyThrows
    @RequestMapping("/callByVistor")
    public Object calByVistor(@RequestParam String expr) {
        try {
            // Debug log for incoming expression
            System.out.println("Received expression: " + expr);

            // Handle URL encoded newline characters
            expr = expr.replace("%20", " ").replace("%5Cn", "\n");
            System.out.println("Processed expression: " + expr);

            // Initialize ANTLR components
            ANTLRInputStream input = new ANTLRInputStream(expr);
            LabeledExprLexer lexer = new LabeledExprLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LabeledExprParser parser = new LabeledExprParser(tokens);
            ParseTree tree = parser.prog(); // parse

            // Debug log for parse tree
            System.out.println("Parse tree: " + tree.toStringTree(parser));

            // Initialize EvalVisitor
            EvalVisitor eval = new EvalVisitor();
            return eval.visit(tree);
        } catch (Exception e) {
            log.error("Error occurred while evaluating expression: {}", e.getMessage());
            throw e; // Or handle the error in a more appropriate way
        }
    }

    @SneakyThrows
    @GetMapping("/callByListener")
    public Object calByListener(@RequestParam String expr) {
        try {
            // Handle URL encoded newline characters
            expr = expr.replace("%20", " ").replace("%5Cn", "\n");
            InputStream is = new ByteArrayInputStream(expr.getBytes());
            ANTLRInputStream input = new ANTLRInputStream(is);
            LabeledExprLexer lexer = new LabeledExprLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            LabeledExprParser parser = new LabeledExprParser(tokens);
            ParseTree tree = parser.prog(); // parse

            ParseTreeWalker walker = new ParseTreeWalker();
            EvalListener listener = new EvalListener();
            walker.walk(listener, tree);

            // Assuming the listener has a method to get the result of the last expression
            return String.valueOf(listener.getResult());
        } catch (Exception e) {
            log.error("Error occurred while evaluating expression: {}", e.getMessage());
            throw e;
        }
    }
}