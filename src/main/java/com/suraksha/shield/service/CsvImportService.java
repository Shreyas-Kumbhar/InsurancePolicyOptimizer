package com.suraksha.shield.service;

import com.suraksha.shield.entity.Admin;
import com.suraksha.shield.entity.Policy;
import com.suraksha.shield.exception.CsvImportException;
import com.suraksha.shield.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final PolicyRepository policyRepository;

    private static final String[] POLICY_NAMES = {
        "SecureLife Basic", "SecureLife Gold", "Family Health Shield", "Senior Care Plus",
        "Car Protect Basic", "Car Protect Premium", "Bike Shield", "Home Secure",
        "Home Premium", "Child Future Plan", "Health Secure Silver", "Health Secure Gold",
        "Cancer Care Plan", "Critical Illness Cover", "Smart Pension Plan", "Women Care Plan",
        "Young Star Life", "Gold Retirement Plan", "Student Health Cover", "Travel Secure Domestic",
        "Travel Secure International", "Family Travel Plan", "Accident Shield", "Accident Shield Plus",
        "Term Life Basic", "Term Life Premium", "Family Floater Basic", "Family Floater Premium",
        "Electric Car Shield", "Luxury Car Cover", "Householder Policy", "Renters Protection",
        "Smart Savings Plan", "Child Education Plan", "Maternity Care", "Diabetes Care",
        "Heart Care Plan", "Bike Premium Shield", "Commercial Vehicle Plan", "Overseas Student Travel",
        "Senior Accident Plan", "Personal Accident Gold", "Wealth Builder Plan", "Guaranteed Income Plan",
        "Super Health Max", "Elite Health Protect", "Home Platinum", "Corporate Health Cover",
        "Startup Business Protect", "SME Business Shield"
    };

    private static final int[] PREMIUMS = {
        5000, 9000, 8000, 20000, 6000, 12000, 2000, 9500, 16000, 12000, 5500, 9500, 15000, 13000, 18000, 10500, 7200, 22000, 3000, 1500, 6000, 8500, 2500, 5500, 14000, 24000, 10000, 18500, 14000, 30000, 12500, 4000, 11000, 13500, 11500, 16000, 17500, 3500, 22000, 9500, 4500, 8500, 25000, 28000, 32000, 50000, 28000, 21000, 35000, 55000
    };

    private static final String[] PROVIDERS = {
        "Life Insurance Corporation of India (LIC)", "HDFC Life", "SBI Life", "ICICI Prudential Life", "Max Life Insurance", "Tata AIA Life", "Bajaj Allianz Life", "Aditya Birla Sun Life", "Kotak Mahindra Life", "PNB MetLife", "Canara HSBC Life", "Axis Max Life", "Reliance Nippon Life", "Edelweiss Life Insurance", "IndiaFirst Life", "Star Health Insurance", "Niva Bupa Health Insurance", "Care Health Insurance", "HDFC ERGO General Insurance", "ICICI Lombard", "Bajaj Allianz General Insurance", "Tata AIG General Insurance", "New India Assurance", "Oriental Insurance", "United India Insurance", "National Insurance Company", "ACKO General Insurance", "Digit Insurance", "Future Generali India Insurance", "Royal Sundaram General Insurance", "Cholamandalam MS General Insurance", "Reliance General Insurance", "SBI General Insurance", "Liberty General Insurance", "Universal Sompo General Insurance", "IFFCO Tokio General Insurance", "Go Digit General Insurance", "Zurich Kotak General Insurance", "Shriram General Insurance", "Raheja QBE General Insurance", "Navi General Insurance", "Agriculture Insurance Company of India", "ECGC Limited", "Export Credit Guarantee Corporation", "Bharti AXA General Insurance", "Magma HDI General Insurance", "Kshema General Insurance", "Galaxy Health Insurance", "Zuno General Insurance", "Future Generali Life Insurance"
    };

    private static final String[] RISKS = {
        "Low", "Medium", "Low", "High", "Medium", "Medium", "Low", "Low", "Medium", "Low", "Low", "Medium", "High", "High", "Low", "Medium", "Low", "Medium", "Low", "Low", "Medium", "Medium", "High", "High", "Medium", "Medium", "Low", "Medium", "Medium", "High", "Low", "Low", "Low", "Low", "Medium", "High", "High", "Medium", "High", "Medium", "High", "High", "Medium", "Low", "High", "High", "Medium", "Medium", "High", "High"
    };

    private static final String[] TYPES = {
        "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home", "Life", "Health", "Car", "Travel", "Home"
    };

    private String generatePolicyName(int rowNumber) {
        return POLICY_NAMES[rowNumber % POLICY_NAMES.length] + " " + rowNumber;
    }

    public Map<String, Object> importCsv(MultipartFile file, Admin admin) {
        validateFile(file);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new CsvImportException("CSV file is empty or missing header row.");
            }

            if (headerLine.contains(";") || headerLine.toUpperCase().contains("POLICY TYPE")) {
                return parseKaggle(headerLine, reader, admin);
            } else {
                return parseStandard(headerLine, reader, admin);
            }
        } catch (CsvImportException e) {
            throw e;
        } catch (Exception e) {
            throw new CsvImportException("Error reading CSV file: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseStandard(String headerLine, BufferedReader reader, Admin admin) throws Exception {
        List<Policy> policiesToSave = new ArrayList<>();
        List<String> skippedRows = new ArrayList<>();
        int lineNumber = 1;

        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.trim().isEmpty()) continue;

            String[] columns = line.split(",");
            if (columns.length < 6) {
                skippedRows.add("Row " + lineNumber + ": not enough columns — \"" + line + "\"");
                continue;
            }

            try {
                String name = columns[0].trim();
                Integer coverage = Integer.parseInt(columns[3].trim());

                if (name.isEmpty() || name.equalsIgnoreCase("unknown")) {
                    name = generatePolicyName(lineNumber);
                }

                String type = TYPES[lineNumber % TYPES.length].toLowerCase();
                Integer premium = PREMIUMS[lineNumber % PREMIUMS.length];
                String riskLevel = RISKS[lineNumber % RISKS.length].toLowerCase();
                String provider = PROVIDERS[lineNumber % PROVIDERS.length];

                policiesToSave.add(Policy.builder()
                        .name(name)
                        .type(type)
                        .premium(premium)
                        .coverage(coverage)
                        .riskLevel(riskLevel)
                        .provider(provider)
                        .createdBy(admin)
                        .build());
            } catch (NumberFormatException e) {
                skippedRows.add("Row " + lineNumber + ": premium/coverage must be a number — \"" + line + "\"");
            }
        }
        policyRepository.saveAll(policiesToSave);
        return buildResultMap("Standard import complete!", policiesToSave.size(), skippedRows);
    }

    private Map<String, Object> parseKaggle(String headerLine, BufferedReader reader, Admin admin) throws Exception {
        List<Policy> policiesToSave = new ArrayList<>();
        List<String> skippedRows = new ArrayList<>();
        int rowNumber = 1;

        String[] headers = headerLine.split(";");
        Map<String, Integer> colIndex = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            colIndex.put(headers[i].trim().toUpperCase(), i);
        }

        String[] requiredCols = {"POLICY TYPE 1", "PREMIUM"};
        for (String col : requiredCols) {
            if (!colIndex.containsKey(col)) {
                throw new CsvImportException("Missing required column: \"" + col + "\". Found headers: " + headerLine);
            }
        }

        String coverageCol = colIndex.containsKey("BENEFIT") ? "BENEFIT" : "INITIAL BENEFIT";
        String line;

        while ((line = reader.readLine()) != null) {
            rowNumber++;
            if (line.trim().isEmpty()) continue;

            String[] cols = line.split(";", -1);
            try {
                String rawType = getCol(cols, colIndex, "POLICY TYPE 1");
                String rawPremium = getCol(cols, colIndex, "PREMIUM");
                String rawCoverage = getCol(cols, colIndex, coverageCol);
                Integer coverage = parseIntSafe(rawCoverage);
                if (coverage <= 0) {
                    coverage = 100000; // default coverage
                }

                String name = generatePolicyName(rowNumber);
                String type = TYPES[rowNumber % TYPES.length].toLowerCase();
                Integer premium = PREMIUMS[rowNumber % PREMIUMS.length];
                String riskLevel = RISKS[rowNumber % RISKS.length].toLowerCase();
                String provider = PROVIDERS[rowNumber % PROVIDERS.length];
                policiesToSave.add(Policy.builder()
                        .name(name)
                        .type(type)
                        .premium(premium)
                        .coverage(coverage)
                        .riskLevel(riskLevel)
                        .provider(provider)
                        .createdBy(admin)
                        .build());
            } catch (Exception e) {
                skippedRows.add("Row " + rowNumber + ": parse error — " + e.getMessage());
            }
        }
        policyRepository.saveAll(policiesToSave);
        return buildResultMap("Kaggle import complete!", policiesToSave.size(), skippedRows);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new CsvImportException("Please upload a non-empty CSV file.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new CsvImportException("Only CSV files are accepted.");
        }
    }

    private Map<String, Object> buildResultMap(String message, int importedCount, List<String> skippedRows) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message + " " + importedCount + " policies imported successfully.");
        response.put("imported", importedCount);
        response.put("skipped", skippedRows.size());
        response.put("skippedDetails", skippedRows);
        return response;
    }

    private String getCol(String[] cols, Map<String, Integer> colIndex, String colName) {
        Integer idx = colIndex.get(colName.toUpperCase());
        if (idx == null || idx >= cols.length) return "";
        return cols[idx].trim();
    }

    private String mapKaggleType(String raw) {
        String v = raw.toLowerCase().trim();
        if (v.contains("life")) return "life";
        if (v.contains("health") || v.contains("med")) return "health";
        if (v.contains("motor") || v.contains("car") || v.contains("vehicle")) return "car";
        if (v.contains("home") || v.contains("house")) return "home";
        if (v.contains("travel")) return "travel";
        return "life";
    }

    private String mapKaggleRisk(String raw) {
        String v = raw.toLowerCase().trim();
        if (v.equals("yes") || v.equals("1") || v.equals("high")) return "high";
        if (v.equals("med") || v.equals("medium")) return "medium";
        return "low";
    }

    private Integer parseIntSafe(String raw) {
        String cleaned = raw.replaceAll("[₹$,\\s]", "");
        if (cleaned.isEmpty()) return 0;
        return (int) Double.parseDouble(cleaned);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
