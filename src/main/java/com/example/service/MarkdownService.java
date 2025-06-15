package com.example.service;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Pattern;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;
    
    // Pattern to detect potentially unsafe HTML tags
    private static final Pattern UNSAFE_HTML_PATTERN = Pattern.compile(
        "<\\s*(script|iframe|object|embed|form|input|meta|link)\\b[^>]*>",
        Pattern.CASE_INSENSITIVE
    );

    public MarkdownService() {
        MutableDataSet options = new MutableDataSet();
        
        // Configure extensions for GitHub Flavored Markdown
        options.set(Parser.EXTENSIONS, Arrays.asList(
            AutolinkExtension.create(),      // Auto-link URLs
            StrikethroughExtension.create(), // ~~strikethrough~~
            TablesExtension.create(),        // Tables
            TaskListExtension.create(),      // Task lists
            EmojiExtension.create()          // Emoji support
        ));
        
        // Configure HTML rendering options
        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
        options.set(HtmlRenderer.HARD_BREAK, "<br />\n");
        
        // Security: Escape HTML by default
        options.set(HtmlRenderer.ESCAPE_HTML, true);
        
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * Convert Markdown text to HTML
     */
    public String markdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Parse markdown
            Node document = parser.parse(markdown);
            
            // Render to HTML
            String html = renderer.render(document);
            
            // Additional security check
            html = sanitizeHtml(html);
            
            return html;
        } catch (Exception e) {
            // If markdown parsing fails, return escaped plain text
            return escapeHtml(markdown);
        }
    }

    /**
     * Check if text contains Markdown syntax
     */
    public boolean containsMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        // Simple heuristics to detect common Markdown patterns
        return text.contains("**") ||     // Bold
               text.contains("*") ||      // Italic
               text.contains("#") ||      // Headers
               text.contains("[") ||      // Links
               text.contains("`") ||      // Code
               text.contains("~~");       // Strikethrough
    }

    /**
     * Get a plain text preview of markdown content
     */
    public String getPlainTextPreview(String markdown, int maxLength) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        
        // Remove common markdown syntax for preview
        String plainText = markdown
            .replaceAll("\\*\\*(.*?)\\*\\*", "$1")  // Bold
            .replaceAll("\\*(.*?)\\*", "$1")        // Italic
            .replaceAll("~~(.*?)~~", "$1")          // Strikethrough
            .replaceAll("`(.*?)`", "$1")            // Inline code
            .replaceAll("#{1,6}\\s+", "")           // Headers
            .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1") // Links
            .trim();
        
        if (plainText.length() > maxLength) {
            return plainText.substring(0, maxLength) + "...";
        }
        
        return plainText;
    }

    /**
     * Sanitize HTML to remove potentially unsafe content
     */
    private String sanitizeHtml(String html) {
        if (html == null) {
            return "";
        }
        
        // Remove potentially unsafe HTML tags
        html = UNSAFE_HTML_PATTERN.matcher(html).replaceAll("");
        
        return html;
    }

    /**
     * Escape HTML characters
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
} 