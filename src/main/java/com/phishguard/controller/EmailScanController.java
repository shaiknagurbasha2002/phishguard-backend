package com.phishguard.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.phishguard.model.EmailScan;
import com.phishguard.repository.EmailScanRepository;

@RestController
@RequestMapping("/api/email-scans")
public class EmailScanController {

    @Autowired
    private EmailScanRepository emailScanRepository;

    @GetMapping
    public List<EmailScan> getAllScans() {
        return emailScanRepository.findAll();
    }

    @GetMapping("/user/{userId}")
    public List<EmailScan> getScansByUser(@PathVariable Long userId) {
        return emailScanRepository.findByUserId(userId);
    }

    @PostMapping
    public EmailScan createScan(@RequestBody EmailScan scan) {
        if (scan.getScannedAt() == null) {
            scan.setScannedAt(LocalDateTime.now());
        }

        String text = (
            String.valueOf(scan.getSubject()) + " " + String.valueOf(scan.getContent())
        ).toLowerCase();

        // Calculate risk score (0-100)
        int score = 0;
        java.util.List<String> findings = new java.util.ArrayList<>();
        java.util.List<String> suspicious = new java.util.ArrayList<>();

        if (text.contains("password")) { score += 30; suspicious.add("Password request detected"); findings.add("{\"type\":\"critical\",\"title\":\"Password Solicitation\",\"description\":\"This email requests your password. Legitimate services never ask for passwords via email.\"}"); }
        if (text.contains("urgent") || text.contains("immediately")) { score += 20; suspicious.add("Urgency tactics"); findings.add("{\"type\":\"warning\",\"title\":\"Urgency Language\",\"description\":\"The email uses urgent language to pressure you into acting quickly without thinking.\"}"); }
        if (text.contains("click here") || text.contains("click now")) { score += 25; suspicious.add("Suspicious link text"); findings.add("{\"type\":\"critical\",\"title\":\"Suspicious Link\",\"description\":\"Vague link text like 'click here' is a common phishing tactic.\"}"); }
        if (text.contains("verify") || text.contains("confirm your account")) { score += 20; suspicious.add("Account verification request"); findings.add("{\"type\":\"warning\",\"title\":\"Account Verification Request\",\"description\":\"Phishing emails often ask you to verify your account details.\"}"); }
        if (text.contains("login") || text.contains("sign in")) { score += 15; suspicious.add("Login credential request"); findings.add("{\"type\":\"warning\",\"title\":\"Login Request\",\"description\":\"Be cautious of emails asking you to log in via a link.\"}"); }
        if (text.contains("prize") || text.contains("won") || text.contains("lottery")) { score += 35; suspicious.add("Prize/lottery scam"); findings.add("{\"type\":\"critical\",\"title\":\"Prize Scam Indicator\",\"description\":\"References to prizes or lotteries are classic social engineering tactics.\"}"); }
        if (text.contains("bank") || text.contains("credit card") || text.contains("payment")) { score += 25; suspicious.add("Financial information request"); findings.add("{\"type\":\"critical\",\"title\":\"Financial Data Request\",\"description\":\"This email references banking or payment information.\"}"); }
        if (text.contains("http://") && !text.contains("https://")) { score += 20; suspicious.add("Unencrypted HTTP link"); findings.add("{\"type\":\"warning\",\"title\":\"Unencrypted Link\",\"description\":\"The email contains an HTTP (not HTTPS) link, which is insecure.\"}"); }

        if (score == 0) {
            findings.add("{\"type\":\"info\",\"title\":\"No Obvious Threats Detected\",\"description\":\"The content does not match common phishing patterns, but always stay vigilant.\"}");
        }

        if (score > 100) score = 100;
        scan.setRiskScore(score);

        if (scan.getRiskLevel() == null || scan.getRiskLevel().isBlank()) {
            if (score >= 70) { scan.setRiskLevel("HIGH"); scan.setStatus("dangerous"); }
            else if (score >= 40) { scan.setRiskLevel("MEDIUM"); scan.setStatus("suspicious"); }
            else { scan.setRiskLevel("LOW"); scan.setStatus("safe"); }
        }

        if (scan.getStatus() == null || scan.getStatus().isBlank()) {
            if (score >= 70) scan.setStatus("dangerous");
            else if (score >= 40) scan.setStatus("suspicious");
            else scan.setStatus("safe");
        }

        // Generate AI summary
        String summary;
        if (score >= 70) {
            summary = "HIGH RISK: This content shows multiple strong indicators of a phishing attempt. It contains " + suspicious.size() + " suspicious element(s) including " + (suspicious.isEmpty() ? "unknown patterns" : String.join(", ", suspicious)) + ". Do NOT click any links, provide personal information, or respond to this message.";
        } else if (score >= 40) {
            summary = "MEDIUM RISK: This content has some characteristics that may indicate a phishing attempt. Found " + suspicious.size() + " potential indicator(s): " + (suspicious.isEmpty() ? "none" : String.join(", ", suspicious)) + ". Proceed with caution and verify the sender independently.";
        } else {
            summary = "LOW RISK: No obvious phishing indicators detected in this content. The email appears relatively safe, but always verify the sender's identity before clicking links or providing information.";
        }
        scan.setAiSummary(summary);
        scan.setFindingsJson("[" + String.join(",", findings) + "]");
        scan.setSuspiciousElementsJson("[" + suspicious.stream().map(s -> "\"" + s + "\"").collect(java.util.stream.Collectors.joining(",")) + "]");

        return emailScanRepository.save(scan);
    }
}