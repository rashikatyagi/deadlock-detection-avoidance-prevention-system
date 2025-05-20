
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.PrintStream;

/**
 * GUI for Deadlock Management System
 * This class provides a graphical interface for the deadlock algorithms implemented in latest.java
 */
public class DeadlockGUI extends JFrame {
    
    // Main components
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JPanel configPanel;
    private JPanel resultPanel;
    
    // Configuration inputs
    private JSpinner processCountSpinner;
    private JSpinner resourceCountSpinner;
    private JButton configureButton;
    private JTable maxNeedsTable;
    private JTable allocationTable;
    private JTable availableResourcesTable;
    
    // Result components
    private JTextArea resultTextArea;
    private JButton avoidanceButton;
    private JButton detectionButton;
    private JButton preventionButton;
    
    // Data models
    private DefaultTableModel maxNeedsModel;
    private DefaultTableModel allocationModel;
    private DefaultTableModel availableResourcesModel;
    
    // System data
    private int numProcesses;
    private int numResources;
    private int[][] maxNeeds;
    private int[][] allocation;
    private int[] totalResources;
    
    // Scroll panes for tables
    private JScrollPane maxNeedsScrollPane;
    private JScrollPane allocationScrollPane;
    private JScrollPane availableScrollPane;
    
    /**
     * Constructor
     */
    public DeadlockGUI() {
        setTitle("Deadlock Management System");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
        
        setVisible(true);
    }
    
    /**
     * Initialize all UI components
     */
    private void initComponents() {
        // Initialize main containers
        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        configPanel = new JPanel(new BorderLayout());
        resultPanel = new JPanel(new BorderLayout());
        
        // Initialize configuration components
        JPanel configInputPanel = new JPanel(new GridBagLayout());
        configInputPanel.setBorder(BorderFactory.createTitledBorder("System Configuration"));
        
        processCountSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        resourceCountSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        configureButton = new JButton("Configure Tables");
        
        // Initialize table models with default sizes
        numProcesses = 3; // Default starting values
        numResources = 3;
        
        initializeTableModels();
        
        // Initialize result components
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        avoidanceButton = new JButton("Check Deadlock Avoidance");
        detectionButton = new JButton("Check Deadlock Detection");
        preventionButton = new JButton("Check Deadlock Prevention");
        
        // Event listeners
        configureButton.addActionListener(e -> configureSystem());
        avoidanceButton.addActionListener(e -> checkDeadlockAvoidance());
        detectionButton.addActionListener(e -> checkDeadlockDetection());
        preventionButton.addActionListener(e -> checkDeadlockPrevention());
    }
    
    /**
     * Initialize table models with proper column identifiers
     */
    private void initializeTableModels() {
        // Create resource column identifiers for the tables
        Vector<String> colIdentifiers = new Vector<>();
        for (int i = 0; i < numResources; i++) {
            colIdentifiers.add("R" + i);
        }
        
        maxNeedsModel = new DefaultTableModel(colIdentifiers, numProcesses) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        
        allocationModel = new DefaultTableModel(colIdentifiers, numProcesses) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        
        availableResourcesModel = new DefaultTableModel(colIdentifiers, 1) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        
        // Initialize tables with default values (0)
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                maxNeedsModel.setValueAt(0, i, j);
                allocationModel.setValueAt(0, i, j);
            }
        }
        
        for (int j = 0; j < numResources; j++) {
            availableResourcesModel.setValueAt(0, 0, j);
        }
        
        // Initialize tables with models
        maxNeedsTable = new JTable(maxNeedsModel);
        allocationTable = new JTable(allocationModel);
        availableResourcesTable = new JTable(availableResourcesModel);
        
        // Create scroll panes for tables
        maxNeedsScrollPane = new JScrollPane(maxNeedsTable);
        allocationScrollPane = new JScrollPane(allocationTable);
        availableScrollPane = new JScrollPane(availableResourcesTable);
    }
    
    /**
     * Layout all components
     */
    private void layoutComponents() {
        // Config panel layout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Number of Processes:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(processCountSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(new JLabel("Number of Resources:"), gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(resourceCountSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(configureButton, gbc);
        
        // Table panel layout
        JPanel tablesPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Max needs table
        JPanel maxNeedsPanel = new JPanel(new BorderLayout());
        maxNeedsPanel.setBorder(BorderFactory.createTitledBorder("Maximum Needs Matrix"));
        maxNeedsPanel.add(maxNeedsScrollPane, BorderLayout.CENTER);
        
        // Allocation table
        JPanel allocationPanel = new JPanel(new BorderLayout());
        allocationPanel.setBorder(BorderFactory.createTitledBorder("Current Allocation Matrix"));
        allocationPanel.add(allocationScrollPane, BorderLayout.CENTER);
        
        // Available resources table
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Total Resource Instances"));
        availablePanel.add(availableScrollPane, BorderLayout.CENTER);
        
        tablesPanel.add(maxNeedsPanel);
        tablesPanel.add(allocationPanel);
        tablesPanel.add(availablePanel);
        
        // Combine input and tables in config panel
        configPanel.add(inputPanel, BorderLayout.NORTH);
        configPanel.add(tablesPanel, BorderLayout.CENTER);
        
        // Result panel layout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(avoidanceButton);
        buttonPanel.add(detectionButton);
        buttonPanel.add(preventionButton);
        
        resultPanel.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        resultPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add panels to tabbed pane
        tabbedPane.addTab("Configuration", configPanel);
        tabbedPane.addTab("Results", resultPanel);
        
        // Add tabbed pane to main panel
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // Now that tables are in scroll panes and added to the UI, we can add row headers
        updateRowHeaders();
    }
    
    /**
     * Configure the system based on user inputs
     */
    private void configureSystem() {
        numProcesses = (Integer) processCountSpinner.getValue();
        numResources = (Integer) resourceCountSpinner.getValue();
        
        // Create resource column identifiers
        Vector<String> colIdentifiers = new Vector<>();
        for (int i = 0; i < numResources; i++) {
            colIdentifiers.add("R" + i);
        }
        
        // Reset table models with new dimensions
        maxNeedsModel = new DefaultTableModel(colIdentifiers, numProcesses) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        maxNeedsTable.setModel(maxNeedsModel);
        
        allocationModel = new DefaultTableModel(colIdentifiers, numProcesses) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        allocationTable.setModel(allocationModel);
        
        availableResourcesModel = new DefaultTableModel(colIdentifiers, 1) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return Integer.class;
            }
        };
        availableResourcesTable.setModel(availableResourcesModel);
        
        // Initialize cells with 0 values
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numResources; j++) {
                maxNeedsModel.setValueAt(0, i, j);
                allocationModel.setValueAt(0, i, j);
            }
        }
        
        for (int j = 0; j < numResources; j++) {
            availableResourcesModel.setValueAt(0, 0, j);
        }
        
        // Update table headers and row headers
        updateHeadersAndProperties();
        
        // Show a confirmation message
        JOptionPane.showMessageDialog(this, 
            "Tables configured for " + numProcesses + " processes and " + numResources + " resources.\n" +
            "Please fill in the tables with appropriate values.",
            "Configuration Complete", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Update headers and table properties after configuration
     */
    private void updateHeadersAndProperties() {
        // Set table header properties
        JTableHeader header = maxNeedsTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        
        header = allocationTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        
        header = availableResourcesTable.getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        
        // Set row heights
        maxNeedsTable.setRowHeight(25);
        allocationTable.setRowHeight(25);
        availableResourcesTable.setRowHeight(25);
        
        // Update row headers
        updateRowHeaders();
    }
    
    /**
     * Update row headers for all tables
     */
    private void updateRowHeaders() {
        // Create process row identifiers
        Vector<String> rowIdentifiers = new Vector<>();
        for (int i = 0; i < numProcesses; i++) {
            rowIdentifiers.add("P" + i);
        }
        
        // Create row header tables
        JTable maxNeedsRowHeader = createRowHeaderTable(rowIdentifiers);
        JTable allocationRowHeader = createRowHeaderTable(rowIdentifiers);
        
        // Create a simple "Total" label for the available resources row header
        Vector<String> totalLabel = new Vector<>();
        totalLabel.add("Total");
        JTable availableRowHeader = createRowHeaderTable(totalLabel);
        
        // Set row heights to match between tables
        maxNeedsRowHeader.setRowHeight(25);
        allocationRowHeader.setRowHeight(25);
        availableRowHeader.setRowHeight(25);
        
        // Add row headers to scroll panes
        maxNeedsScrollPane.setRowHeaderView(maxNeedsRowHeader);
        allocationScrollPane.setRowHeaderView(allocationRowHeader);
        availableScrollPane.setRowHeaderView(availableRowHeader);
    }
    
    /**
     * Create a table to use as row header
     */
    private JTable createRowHeaderTable(Vector<String> rowNames) {
        // Create a single column table model for row headers
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Process", rowNames);
        
        // Create the table and set properties
        JTable table = new JTable(model);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(50, 0));
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.setBackground(new Color(240, 240, 240));
        
        return table;
    }
    
    /**
     * Collect data from tables and validate inputs
     */
    private boolean collectAndValidateData() {
        try {
            maxNeeds = new int[numProcesses][numResources];
            allocation = new int[numProcesses][numResources];
            totalResources = new int[numResources];
            
            // Collect max needs data
            for (int i = 0; i < numProcesses; i++) {
                for (int j = 0; j < numResources; j++) {
                    Object value = maxNeedsTable.getValueAt(i, j);
                    if (value == null) {
                        maxNeeds[i][j] = 0; // Default to 0 instead of throwing error
                    } else {
                        try {
                            maxNeeds[i][j] = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            maxNeeds[i][j] = 0; // Convert non-integer to 0
                        }
                    }
                    if (maxNeeds[i][j] < 0) {
                        throw new IllegalArgumentException("Negative value in max needs table at row " + i + ", column " + j);
                    }
                }
            }
            
            // Collect allocation data
            for (int i = 0; i < numProcesses; i++) {
                for (int j = 0; j < numResources; j++) {
                    Object value = allocationTable.getValueAt(i, j);
                    if (value == null) {
                        allocation[i][j] = 0; // Default to 0 instead of throwing error
                    } else {
                        try {
                            allocation[i][j] = Integer.parseInt(value.toString());
                        } catch (NumberFormatException e) {
                            allocation[i][j] = 0; // Convert non-integer to 0
                        }
                    }
                    if (allocation[i][j] < 0) {
                        throw new IllegalArgumentException("Negative value in allocation table at row " + i + ", column " + j);
                    }
                    
                    // Check if allocation exceeds max need
                    if (allocation[i][j] > maxNeeds[i][j]) {
                        throw new IllegalArgumentException(
                            "Allocation exceeds max need for process P" + i + " and resource R" + j);
                    }
                }
            }
            
            // Collect total resources data
            for (int j = 0; j < numResources; j++) {
                Object value = availableResourcesTable.getValueAt(0, j);
                if (value == null) {
                    totalResources[j] = 0; // Default to 0 instead of throwing error
                } else {
                    try {
                        totalResources[j] = Integer.parseInt(value.toString());
                    } catch (NumberFormatException e) {
                        totalResources[j] = 0; // Convert non-integer to 0
                    }
                }
                if (totalResources[j] < 0) {
                    throw new IllegalArgumentException("Negative value in total resources table at column " + j);
                }
            }
            
            // Validate that allocation doesn't exceed total resources
            for (int j = 0; j < numResources; j++) {
                int totalAllocated = 0;
                for (int i = 0; i < numProcesses; i++) {
                    totalAllocated += allocation[i][j];
                }
                if (totalAllocated > totalResources[j]) {
                    throw new IllegalArgumentException(
                        "Total allocation exceeds available instances for resource R" + j + 
                        " (allocated: " + totalAllocated + ", total: " + totalResources[j] + ")");
                }
            }
            
            return true;
        }
        catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid input: " + e.getMessage(), 
                "Input Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "An unexpected error occurred: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Calculate available resources after allocation
     */
    private int[] calculateAvailableResources() {
        int[] available = new int[numResources];
        for (int j = 0; j < numResources; j++) {
            int totalAllocated = 0;
            for (int i = 0; i < numProcesses; i++) {
                totalAllocated += allocation[i][j];
            }
            available[j] = totalResources[j] - totalAllocated;
        }
        return available;
    }
    
    /**
     * Check deadlock avoidance using Banker's Algorithm
     */
    private void checkDeadlockAvoidance() {
        if (!collectAndValidateData()) {
            return;
        }
        
        // Switch to results tab
        tabbedPane.setSelectedIndex(1);
        
        // Reset result area
        resultTextArea.setText("");
        resultTextArea.append("=== Deadlock Avoidance (Banker's Algorithm) ===\n\n");
        
        // Create system configuration
        List<Process> processes = new ArrayList<>();
        List<Resource> resources = new ArrayList<>();
        
        try {
            // Create resources
            for (int i = 0; i < numResources; i++) {
                resources.add(new Resource("R" + i, totalResources[i]));
            }
            
            // Create processes and set max needs
            for (int i = 0; i < numProcesses; i++) {
                Process p = new Process("P" + i);
                for (int j = 0; j < numResources; j++) {
                    p.setMaxNeed(resources.get(j), maxNeeds[i][j]);
                }
                processes.add(p);
            }
            
            // Calculate need matrix
            int[][] needMatrix = new int[numProcesses][numResources];
            for (int i = 0; i < numProcesses; i++) {
                for (int j = 0; j < numResources; j++) {
                    needMatrix[i][j] = maxNeeds[i][j] - allocation[i][j];
                }
            }
            
            // Initialize resource allocations
            updateResourceAvailableUnits(resources, allocation, processes);
            
            // Display the current state
            resultTextArea.append("Current System State:\n");
            resultTextArea.append("--------------------\n");
            resultTextArea.append("Maximum Needs Matrix:\n");
            appendMatrix(resultTextArea, maxNeeds);
            resultTextArea.append("\nCurrent Allocation Matrix:\n");
            appendMatrix(resultTextArea, allocation);
            resultTextArea.append("\nAvailable Resources:\n");
            int[] available = calculateAvailableResources();
            for (int j = 0; j < numResources; j++) {
                resultTextArea.append("R" + j + ": " + available[j] + " ");
            }
            resultTextArea.append("\n\n");
            
            // Run Banker's Algorithm
            resultTextArea.append("Running Banker's Algorithm...\n\n");
            BankersAlgorithm banker = new BankersAlgorithm(processes, resources, needMatrix);
            boolean isSafe = banker.checkSafeState();
            
            if (isSafe) {
                resultTextArea.append("\nResult: The system is in a SAFE state.\n");
            } else {
                resultTextArea.append("\nResult: The system is in an UNSAFE state.\n");
                resultTextArea.append("Deadlock may occur if resource requests are granted.\n");
            }
        } catch (Exception e) {
            resultTextArea.append("\nAn error occurred during execution: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * Check deadlock detection
     */
    private void checkDeadlockDetection() {
        if (!collectAndValidateData()) {
            return;
        }
        
        // Switch to results tab
        tabbedPane.setSelectedIndex(1);
        
        // Reset result area
        resultTextArea.setText("");
        resultTextArea.append("=== Deadlock Detection ===\n\n");
        
        try {
            // Create system configuration
            List<Process> processes = new ArrayList<>();
            List<Resource> resources = new ArrayList<>();
            
            // Create resources
            for (int i = 0; i < numResources; i++) {
                resources.add(new Resource("R" + i, totalResources[i]));
            }
            
            // Create processes and set max needs
            for (int i = 0; i < numProcesses; i++) {
                Process p = new Process("P" + i);
                for (int j = 0; j < numResources; j++) {
                    p.setMaxNeed(resources.get(j), maxNeeds[i][j]);
                }
                processes.add(p);
            }
            
            // Calculate need matrix
            int[][] needMatrix = new int[numProcesses][numResources];
            for (int i = 0; i < numProcesses; i++) {
                for (int j = 0; j < numResources; j++) {
                    needMatrix[i][j] = maxNeeds[i][j] - allocation[i][j];
                }
            }
            
            // Initialize resource allocations
            updateResourceAvailableUnits(resources, allocation, processes);
            
            // Display the current state
            resultTextArea.append("Current System State:\n");
            resultTextArea.append("--------------------\n");
            resultTextArea.append("Maximum Needs Matrix:\n");
            appendMatrix(resultTextArea, maxNeeds);
            resultTextArea.append("\nCurrent Allocation Matrix:\n");
            appendMatrix(resultTextArea, allocation);
            resultTextArea.append("\nAvailable Resources:\n");
            int[] available = calculateAvailableResources();
            for (int j = 0; j < numResources; j++) {
                resultTextArea.append("R" + j + ": " + available[j] + " ");
            }
            resultTextArea.append("\n\n");
            
            // Determine which detection algorithm to use
            boolean hasSingleInstance = true;
            for (Resource r : resources) {
                if (r.getTotalUnits() > 1) {
                    hasSingleInstance = false;
                    break;
                }
            }
            
            if (hasSingleInstance) {
                resultTextArea.append("Using Wait-For Graph for deadlock detection (single instance resources)\n\n");
                WaitForGraphDetector detector = new WaitForGraphDetector(processes, resources, allocation);
                boolean hasDeadlock = detector.detectDeadlock();
                
                if (hasDeadlock) {
                    resultTextArea.append("\nResult: DEADLOCK DETECTED!\n");
                    resultTextArea.append("Deadlocked processes: " + detector.getDeadlockedProcesses() + "\n");
                } else {
                    resultTextArea.append("\nResult: No deadlock detected.\n");
                }
            } else {
                resultTextArea.append("Using Resource Allocation Graph for deadlock detection (multiple instance resources)\n\n");
                DeadlockDetector detector = new DeadlockDetector(processes, resources, needMatrix);
                boolean hasDeadlock = detector.detectDeadlock();
                
                if (hasDeadlock) {
                    resultTextArea.append("\nResult: DEADLOCK DETECTED!\n");
                    resultTextArea.append("Deadlocked processes: " + detector.getDeadlockedProcesses() + "\n");
                } else {
                    resultTextArea.append("\nResult: No deadlock detected.\n");
                }
            }
        } catch (Exception e) {
            resultTextArea.append("\nAn error occurred during execution: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * Check deadlock prevention
     */
    private void checkDeadlockPrevention() {
        if (!collectAndValidateData()) {
            return;
        }
        
        // Switch to results tab
        tabbedPane.setSelectedIndex(1);
        
        // Reset result area
        resultTextArea.setText("");
        resultTextArea.append("=== Deadlock Prevention Analysis ===\n\n");
        
        try {
            // Display the current state
            resultTextArea.append("Current System State:\n");
            resultTextArea.append("--------------------\n");
            resultTextArea.append("Maximum Needs Matrix:\n");
            appendMatrix(resultTextArea, maxNeeds);
            resultTextArea.append("\nCurrent Allocation Matrix:\n");
            appendMatrix(resultTextArea, allocation);
            resultTextArea.append("\nAvailable Resources:\n");
            int[] available = calculateAvailableResources();
            for (int j = 0; j < numResources; j++) {
                resultTextArea.append("R" + j + ": " + available[j] + " ");
            }
            resultTextArea.append("\n\n");
            
            // Analyze prevention strategies
            resultTextArea.append("Deadlock Prevention Techniques Analysis:\n\n");
            analyzeDeadlockPrevention();
        } catch (Exception e) {
            resultTextArea.append("\nAn error occurred during execution: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to append matrix to text area
     */
    private void appendMatrix(JTextArea textArea, int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            textArea.append("P" + i + ": ");
            for (int j = 0; j < matrix[i].length; j++) {
                textArea.append(String.format("%3d ", matrix[i][j]));
            }
            textArea.append("\n");
        }
    }
    
    /**
     * Analyze deadlock prevention strategies
     */
    private void analyzeDeadlockPrevention() {
        // Check if the system violates conditions for deadlock
        boolean hasResourceHoarding = false;
        boolean hasCircularWait = false;
        boolean hasNoPreemption = true; // Assuming no preemption by default
        
        // Check for resource hoarding (hold and wait)
        for (int i = 0; i < numProcesses; i++) {
            boolean holdingSome = false;
            boolean waitingForOthers = false;
            
            for (int j = 0; j < numResources; j++) {
                if (allocation[i][j] > 0) {
                    holdingSome = true;
                }
                if (maxNeeds[i][j] - allocation[i][j] > 0) {
                    waitingForOthers = true;
                }
            }
            
            if (holdingSome && waitingForOthers) {
                hasResourceHoarding = true;
                break;
            }
        }
        
        // Simplified check for potential circular wait
        // This is a very basic check - a proper implementation would build a wait-for graph
        int[] availableResources = calculateAvailableResources();
        int waitCount = 0;
        List<Integer> waitingProcesses = new ArrayList<>();
        
        for (int i = 0; i < numProcesses; i++) {
            boolean needsUnavailableResource = false;
            
            for (int j = 0; j < numResources; j++) {
                int need = maxNeeds[i][j] - allocation[i][j];
                if (need > 0 && need > availableResources[j]) {
                    needsUnavailableResource = true;
                    break;
                }
            }
            
            if (needsUnavailableResource) {
                waitCount++;
                waitingProcesses.add(i);
            }
        }
        
        // If multiple processes are waiting and can't proceed, potential for circular wait
        if (waitCount > 1) {
            hasCircularWait = true;
        }
        
        // Output analysis 
        resultTextArea.append("1. Mutual Exclusion: Present (resources are non-sharable)\n");
        
        resultTextArea.append("2. Hold and Wait: ");
        if (hasResourceHoarding) {
            resultTextArea.append("Present - processes hold resources while waiting for others\n");
            resultTextArea.append("   Prevention Strategy: Require processes to request all resources at once\n");
        } else {
            resultTextArea.append("Not present - processes either hold all needed resources or none\n");
        }
        
        resultTextArea.append("3. No Preemption: ");
        if (hasNoPreemption) {
            resultTextArea.append("Present - resources cannot be forcibly taken from processes\n");
            resultTextArea.append("   Prevention Strategy: Allow resource preemption in critical situations\n");
        } else {
            resultTextArea.append("Not present - resources can be preempted\n");
        }
        
        resultTextArea.append("4. Circular Wait: ");
        if (hasCircularWait) {
            resultTextArea.append("Potentially present\n");
            resultTextArea.append("   Waiting processes: ");
            for (Integer p : waitingProcesses) {
                resultTextArea.append("P" + p + " ");
            }
            resultTextArea.append("\n");
            resultTextArea.append("   Prevention Strategy: Impose a total ordering of resource types\n");
        } else {
            resultTextArea.append("Not detected\n");
        }
        
        resultTextArea.append("\nDeadlock Prevention Recommendation:\n");
        if (hasResourceHoarding || hasCircularWait) {
            resultTextArea.append("The current system configuration could lead to deadlock.\n");
            
            if (hasResourceHoarding) {
                resultTextArea.append("- Prevent 'Hold and Wait': Modify resource allocation policy to require\n" +
                                    "  processes to request all resources before execution starts.\n");
            }
            
            if (hasCircularWait) {
                resultTextArea.append("- Prevent 'Circular Wait': Implement resource ordering to ensure\n" +
                                    "  processes request resources in a specific order (e.g., lowest ID first).\n");
            }
            
            resultTextArea.append("- Consider implementing resource preemption for critical situations.\n");
        } else {
            resultTextArea.append("The current system configuration is not prone to deadlock based on initial analysis.\n" +
                              "However, monitoring resource allocation patterns is still recommended.\n");
        }
    }
    
    /**
     * Helper function to update resource available units based on allocation
     */
    private void updateResourceAvailableUnits(List<Resource> resources, int[][] allocation, List<Process> processes) {
        // First set all resources as available
        for (Resource r : resources) {
            r.resetAllocations();
        }
        
        // Then allocate based on the allocation matrix
        for (int i = 0; i < numProcesses; i++) {
            Process p = processes.get(i);
            for (int j = 0; j < numResources; j++) {
                if (allocation[i][j] > 0) {
                    resources.get(j).allocateUnits(p, allocation[i][j]);
                }
            }
        }
    }
    
    /**
     * Main method to start the application
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            new DeadlockGUI();
        });
    }
}

/**
 * Represents a process in the system
 */
class Process {
    private String name;
    private Map<Resource, Integer> maxNeeds;
    
    public Process(String name) {
        this.name = name;
        this.maxNeeds = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setMaxNeed(Resource resource, int units) {
        maxNeeds.put(resource, units);
    }
    
    public int getMaxNeed(Resource resource) {
        return maxNeeds.getOrDefault(resource, 0);
    }
    
    @Override
    public String toString() {
        return name;
    }
}

/**
 * Represents a resource in the system
 */
class Resource {
    private String name;
    private int totalUnits;
    Map<Process, Integer> allocations;
    
    public Resource(String name, int totalUnits) {
        this.name = name;
        this.totalUnits = totalUnits;
        this.allocations = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public int getTotalUnits() {
        return totalUnits;
    }
    
    public int getAvailableUnits() {
        int allocated = 0;
        for (int units : allocations.values()) {
            allocated += units;
        }
        return totalUnits - allocated;
    }
    
    public void allocateUnits(Process process, int units) {
        if (units <= 0) {
            return;
        }
        
        int currentAllocation = allocations.getOrDefault(process, 0);
        allocations.put(process, currentAllocation + units);
    }
    
    public void deallocateUnits(Process process, int units) {
        if (!allocations.containsKey(process) || units <= 0) {
            return;
        }
        
        int currentAllocation = allocations.get(process);
        int newAllocation = Math.max(0, currentAllocation - units);
        
        if (newAllocation == 0) {
            allocations.remove(process);
        } else {
            allocations.put(process, newAllocation);
        }
    }
    
    public void resetAllocations() {
        allocations.clear();
    }
    
    @Override
    public String toString() {
        return name;
    }
}

/**
 * Implements Banker's Algorithm for deadlock avoidance
 */
class BankersAlgorithm {
    private List<Process> processes;
    private List<Resource> resources;
    private int[][] needMatrix;
    private int[] work;
    private boolean[] finish;
    private List<Process> safeSequence;
    
    public BankersAlgorithm(List<Process> processes, List<Resource> resources, int[][] needMatrix) {
        this.processes = processes;
        this.resources = resources;
        this.needMatrix = needMatrix;
        this.work = new int[resources.size()];
        this.finish = new boolean[processes.size()];
        this.safeSequence = new ArrayList<>();
        
        // Initialize work array with available resources
        for (int i = 0; i < resources.size(); i++) {
            work[i] = resources.get(i).getAvailableUnits();
        }
    }
    
    public boolean checkSafeState() {
        System.out.println("Starting Banker's Algorithm...");
        System.out.println("Available resources:");
        for (int i = 0; i < resources.size(); i++) {
            System.out.print(resources.get(i).getName() + ":" + work[i] + " ");
        }
        System.out.println();
        
        Arrays.fill(finish, false);
        safeSequence.clear();
        
        int count = 0;
        
        while (count < processes.size()) {
            boolean found = false;
            
            for (int i = 0; i < processes.size(); i++) {
                if (!finish[i]) {
                    boolean canAllocate = true;
                    
                    // Check if all needs can be satisfied
                    for (int j = 0; j < resources.size(); j++) {
                        if (needMatrix[i][j] > work[j]) {
                            canAllocate = false;
                            break;
                        }
                    }
                    
                    if (canAllocate) {
                        // Process can complete, add its resources to work
                        System.out.println("Process " + processes.get(i).getName() + " can complete");
                        for (int j = 0; j < resources.size(); j++) {
                            int allocatedToProcess = 0;
                            for (Process p : processes) {
                                if (p == processes.get(i)) {
                                    allocatedToProcess = resources.get(j).allocations.getOrDefault(p, 0);
                                    break;
                                }
                            }
                            work[j] += allocatedToProcess;
                            System.out.println("Resource " + resources.get(j).getName() + 
                                               " available units updated to " + work[j]);
                        }
                        
                        finish[i] = true;
                        safeSequence.add(processes.get(i));
                        found = true;
                        count++;
                    }
                }
            }
            
            if (!found) {
                // No process can complete with available resources
                System.out.println("No process can complete with current available resources");
                return false;
            }
        }
        
        System.out.println("Safe sequence: " + safeSequence);
        return true;
    }
    
    public List<Process> getSafeSequence() {
        return safeSequence;
    }
}

/**
 * Implements deadlock detection for multiple instance resources
 */
class DeadlockDetector {
    private List<Process> processes;
    private List<Resource> resources;
    private int[][] needMatrix;
    private int[] work;
    private boolean[] finish;
    private List<Process> deadlockedProcesses;
    
    public DeadlockDetector(List<Process> processes, List<Resource> resources, int[][] needMatrix) {
        this.processes = processes;
        this.resources = resources;
        this.needMatrix = needMatrix;
        this.work = new int[resources.size()];
        this.finish = new boolean[processes.size()];
        this.deadlockedProcesses = new ArrayList<>();
        
        // Initialize work array with available resources
        for (int i = 0; i < resources.size(); i++) {
            work[i] = resources.get(i).getAvailableUnits();
        }
    }
    
    public boolean detectDeadlock() {
        Arrays.fill(finish, false);
        deadlockedProcesses.clear();
        
        // Similar to Banker's algorithm but we only consider current allocation
        boolean changed;
        
        do {
            changed = false;
            
            for (int i = 0; i < processes.size(); i++) {
                if (!finish[i]) {
                    boolean canComplete = true;
                    
                    // Check if process can complete with current resources
                    for (int j = 0; j < resources.size(); j++) {
                        if (needMatrix[i][j] > work[j]) {
                            canComplete = false;
                            break;
                        }
                    }
                    
                    if (canComplete) {
                        // Process can complete, add its resources to work
                        for (int j = 0; j < resources.size(); j++) {
                            int allocatedToProcess = 0;
                            for (Process p : processes) {
                                if (p == processes.get(i)) {
                                    allocatedToProcess = resources.get(j).allocations.getOrDefault(p, 0);
                                    break;
                                }
                            }
                            work[j] += allocatedToProcess;
                        }
                        
                        finish[i] = true;
                        changed = true;
                    }
                }
            }
        } while (changed);
        
        // Check for deadlocked processes
        boolean hasDeadlock = false;
        for (int i = 0; i < processes.size(); i++) {
            if (!finish[i]) {
                deadlockedProcesses.add(processes.get(i));
                hasDeadlock = true;
            }
        }
        
        return hasDeadlock;
    }
    
    public List<Process> getDeadlockedProcesses() {
        return deadlockedProcesses;
    }
}

/**
 * Implements deadlock detection using Wait-For Graph for single instance resources
 */
class WaitForGraphDetector {
    private List<Process> processes;
    private List<Resource> resources;
    private int[][] allocation;
    private boolean[][] waitForGraph;
    private boolean[] visited;
    private boolean[] recursionStack;
    private List<Process> deadlockedProcesses;
    
    public WaitForGraphDetector(List<Process> processes, List<Resource> resources, int[][] allocation) {
        this.processes = processes;
        this.resources = resources;
        this.allocation = allocation;
        this.waitForGraph = new boolean[processes.size()][processes.size()];
        this.visited = new boolean[processes.size()];
        this.recursionStack = new boolean[processes.size()];
        this.deadlockedProcesses = new ArrayList<>();
        
        constructWaitForGraph();
    }
    
    private void constructWaitForGraph() {
        // For each resource, if process i needs it and process j holds it,
        // then process i waits for process j
        for (int r = 0; r < resources.size(); r++) {
            // Find which process holds this resource
            int holder = -1;
            for (int i = 0; i < processes.size(); i++) {
                if (allocation[i][r] > 0) {
                    holder = i;
                    break;
                }
            }
            
            // If resource is allocated, check which processes need it
            if (holder != -1) {
                for (int i = 0; i < processes.size(); i++) {
                    if (i != holder && needsResource(i, r)) {
                        waitForGraph[i][holder] = true;
                    }
                }
            }
        }
    }
    
    private boolean needsResource(int processIndex, int resourceIndex) {
        // Check if process needs more of this resource than it currently has
        for (int j = 0; j < processes.size(); j++) {
            Process p = processes.get(j);
            if (j == processIndex) {
                int maxNeed = p.getMaxNeed(resources.get(resourceIndex));
                return maxNeed > allocation[processIndex][resourceIndex];
            }
        }
        return false;
    }
    
    public boolean detectDeadlock() {
        Arrays.fill(visited, false);
        Arrays.fill(recursionStack, false);
        deadlockedProcesses.clear();
        
        // Check for cycles in the wait-for graph
        for (int i = 0; i < processes.size(); i++) {
            if (!visited[i] && isCyclicUtil(i)) {
                return true;
            }
        }
        
        return !deadlockedProcesses.isEmpty();
    }
    
    private boolean isCyclicUtil(int i) {
        if (!visited[i]) {
            visited[i] = true;
            recursionStack[i] = true;
            
            for (int j = 0; j < processes.size(); j++) {
                if (waitForGraph[i][j]) {
                    if (!visited[j] && isCyclicUtil(j)) {
                        deadlockedProcesses.add(processes.get(i));
                        return true;
                    } else if (recursionStack[j]) {
                        deadlockedProcesses.add(processes.get(i));
                        return true;
                    }
                }
            }
        }
        
        recursionStack[i] = false;
        return false;
    }
    
    public List<Process> getDeadlockedProcesses() {
        return deadlockedProcesses;
    }
}
