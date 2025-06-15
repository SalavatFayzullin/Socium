package com.example.util;

import com.example.service.MarkdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("markdownUtil")
public class MarkdownUtil {

    @Autowired
    private MarkdownService markdownService;

    public String toHtml(String markdown) {
        return markdownService.markdownToHtml(markdown);
    }

    public boolean containsMarkdown(String text) {
        return markdownService.containsMarkdown(text);
    }

    public String getPreview(String markdown, int maxLength) {
        return markdownService.getPlainTextPreview(markdown, maxLength);
    }
} 