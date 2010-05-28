/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WorkerFrame.java
 *
 * Created on May 22, 2010, 10:59:23 AM
 */

package org.signserver.admin.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import javax.swing.JList;
import org.jdesktop.application.Action;
import org.jdesktop.application.Task;
import org.signserver.common.InvalidWorkerIdException;
import org.signserver.common.WorkerConfig;
import org.signserver.common.WorkerStatus;

/**
 *
 * @author markus
 */
public class WorkerFrameOld extends javax.swing.JFrame {

    private int[] workerIds;

    private static String[] statusColumns = {
        "Property", "Value"
    };

    private StatusData statusData;

    /** Creates new form WorkerFrame */
    public WorkerFrameOld(int[] workerIds) {
        this.workerIds = workerIds;
        initComponents();

        System.out.println("workerIds: " + Arrays.toString(workerIds));
//        jTable2
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.signserver.admin.gui.SignServerAdminGUIApplication.class).getContext().getResourceMap(WorkerFrameOld.class);
        setTitle(resourceMap.getString("Form.title")); // NOI18N
        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.signserver.admin.gui.SignServerAdminGUIApplication.class).getContext().getActionMap(WorkerFrameOld.class, this);
        jButton1.setAction(actionMap.get("refreshWorkerStatuses")); // NOI18N
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setName("jSplitPane2"); // NOI18N

        jScrollPane2.setMinimumSize(new java.awt.Dimension(27, 100));
        jScrollPane2.setName("jScrollPane2"); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(45, 100));

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "SODSigner", "Sod1", "Sod2", "Sod3", "Sod4", "Sod5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.setName("jList1"); // NOI18N
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jList1);

        jSplitPane2.setTopComponent(jScrollPane2);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jPanel2.border.title"))); // NOI18N
        jPanel2.setName("jPanel2"); // NOI18N

        jTextPane2.setBorder(null);
        jTextPane2.setContentType(resourceMap.getString("jTextPane2.contentType")); // NOI18N
        jTextPane2.setEditable(false);
        jTextPane2.setText(resourceMap.getString("jTextPane2.text")); // NOI18N
        jTextPane2.setName("jTextPane2"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"ID", "71", null},
                {"Name", "Sod1", null},
                {"Token status", "ACTIVE", null},
                {"Signatures:", "0", null},
                {"Signature limit:", "100000", null},
                {"Validity not before:", "2010-05-20", null},
                {"Validity not after:", "2020-05-20", null},
                {"Certificate chain:", "CN=Sod1, O=Document Signer Pecuela 11, C=PE issued by CN=CSCA Pecuela,O=Pecuela MOI,C=PE", "..."}
            },
            new String [] {
                "Property", "Value", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable2.setEnabled(false);
        jTable2.setName("jTable2"); // NOI18N
        jScrollPane4.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jTextPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addContainerGap())
        );

        jSplitPane2.setBottomComponent(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 796, Short.MAX_VALUE)
                    .addComponent(jButton1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged

        if (!evt.getValueIsAdjusting()) {
            final JList list = (JList) evt.getSource();
            final int row = list.getSelectedIndex();
            if (row != -1) {
                displayStatus(row);
            }
        }
    }//GEN-LAST:event_jList1ValueChanged

    @Action(block = Task.BlockingScope.WINDOW)
    public Task refreshWorkerStatuses() {
        return new RefreshWorkerStatusesTask(org.jdesktop.application.Application.getInstance(org.signserver.admin.gui.SignServerAdminGUIApplication.class));
    }

    private class RefreshWorkerStatusesTask extends org.jdesktop.application.Task<Object, Void> {
        RefreshWorkerStatusesTask(org.jdesktop.application.Application app) {
            // Runs on the EDT.  Copy GUI state that
            // doInBackground() depends on from parameters
            // to RefreshWorkerStatusesTask fields, here.
            super(app);
        }
        @Override protected Object doInBackground() {
            // Your Task's code here.  This method runs
            // on a background thread, so don't reference
            // the Swing GUI from here.

            String[] names = new String[workerIds.length];
            String[] newStatuses = new String[workerIds.length];
            Object[][] newStatusData = new String[workerIds.length][];

            for (int i = 0; i < workerIds.length; i++) {

                final int workerId = workerIds[i];
                System.out.println("WorkerId: " + workerId);

                ByteArrayOutputStream out = new ByteArrayOutputStream();

                final WorkerConfig config = SignServerAdminGUIApplication
                    .getWorkerSession().getCurrentWorkerConfig(workerId);

                names[i] = config.getProperty("NAME") + " (" + workerId + ")";
                WorkerStatus status = null;
                try {
                    status = SignServerAdminGUIApplication.getWorkerSession()
                            .getStatus(workerId);
                    status.displayStatus(workerId,
                            new PrintStream(out), false);
                    newStatuses[i] = out.toString();
                } catch (InvalidWorkerIdException ex) {
                    newStatuses[i] = ("No such worker");
                }
            }

            return new StatusData(names, newStatuses, newStatusData);
        }
        @Override protected void succeeded(Object result) {
            // Runs on the EDT.  Update the GUI based on
            // the result computed by doInBackground().

            final StatusData data = (StatusData) result;

            WorkerFrameOld.this.statusData = data;

            jList1.setListData(WorkerFrameOld.this.statusData.getNames());

//            jTable1.
//
//            jTable1.revalidate();
//
//            workersModel.fireTableDataChanged();
        }
    }

    private void displayStatus(final int row) {
        jTextPane2.setText(statusData.getStatuses()[row]);
    }

    private static class StatusData {

        private String[] names;
        private String[] statuses;
        private Object[][] statusData;

        public StatusData(String[] names, String[] statuses,
                Object[][] statusData) {
            this.names = names;
            this.statuses = statuses;
            this.statusData = statusData;
        }

        public String[] getNames() {
            return names;
        }

        public Object[][] getStatusData() {
            return statusData;
        }

        public String[] getStatuses() {
            return statuses;
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables

}
