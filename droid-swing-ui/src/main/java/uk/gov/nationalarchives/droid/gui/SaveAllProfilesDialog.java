/**
 * Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of the The National Archives nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.gov.nationalarchives.droid.gui;

import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JDialog;
import javax.swing.ListModel;

import uk.gov.nationalarchives.droid.gui.widgetwrapper.ProfileSelectionDialog;

/**
 *
 * @author rflitcroft
 */
public class SaveAllProfilesDialog extends JDialog implements ProfileSelectionDialog {

    private static final long serialVersionUID = 4458147031080987135L;

    private List<CheckListCellModel> model = new ArrayList<CheckListCellModel>();
    private int action = CANCEL;
    private List<ProfileForm> profiles;
    private ListModel listModel;
    
    /** 
     * Creates new form SaveAllProfilesDialog.
     * @param owner the owner of theis dialog
     * @param profiles the profiles to appear in the dialog's list 
     */
    public SaveAllProfilesDialog(Frame owner, List<ProfileForm> profiles) {
        super(owner, "Save profiles");
        
        this.profiles = profiles;

        for (ProfileForm profile : profiles) {
            model.add(new CheckListCellModel(profile.getName(), true));
        }

        listModel = new AbstractListModel() {

            private static final long serialVersionUID = -3790566823193174377L;

            @Override
            public int getSize() {
                return model.size();
            }

            @Override
            public Object getElementAt(int index) {
                return model.get(index);
            }
        };

        initComponents();


        jList1.setCellRenderer(new CheckListRenderer());
        jList1.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = jList1.locationToIndex(e.getPoint());
                CheckListCellModel cell = model.get(index);
                cell.toggleSelection();
                Rectangle rect = jList1.getCellBounds(index, index);
                jList1.repaint(rect);
            }
        });
        
        setLocationRelativeTo(owner);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        instructionLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        dontsaveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(SaveAllProfilesDialog.class, "SaveAllProfilesDialog.cancelButton.text_1")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        saveButton.setText(org.openide.util.NbBundle.getMessage(SaveAllProfilesDialog.class, "SaveAllProfilesDialog.saveButton.text_1")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        instructionLabel.setText(org.openide.util.NbBundle.getMessage(SaveAllProfilesDialog.class, "SaveAllProfilesDialog.instructionLabel.text")); // NOI18N

        jList1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jList1.setModel(listModel);
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(jList1);

        dontsaveButton.setText(org.openide.util.NbBundle.getMessage(SaveAllProfilesDialog.class, "SaveAllProfilesDialog.dontsaveButton.text")); // NOI18N
        dontsaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dontsaveButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addComponent(instructionLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(dontsaveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(instructionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dontsaveButton)
                    .addComponent(cancelButton)
                    .addComponent(saveButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        action = YES;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        action = CANCEL;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void dontsaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dontsaveButtonActionPerformed
        setSaveState(false);
        action = YES;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_dontsaveButtonActionPerformed

    private void setSaveState(boolean saveState) {
        for (int profileIndex = 0; profileIndex < model.size(); profileIndex++) {
            CheckListCellModel profile = model.get(profileIndex);
            profile.setSelection(saveState);
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton dontsaveButton;
    private javax.swing.JLabel instructionLabel;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getResponse() {
        return action;
    }

    @Override
    public List<ProfileForm> getSelectedProfiles() {

        List<ProfileForm> selectedProfiles = new ArrayList<ProfileForm>();
        for (int profileIndex = 0; profileIndex < model.size(); profileIndex++) {
            CheckListCellModel cell = model.get(profileIndex);
            if (cell.isSelected()) {
                selectedProfiles.add(profiles.get(profileIndex));
            }
        }

        return selectedProfiles;
    }
    
    @Override
    public void open() {
        setVisible(true);
    }

    /**
     * @return the model
     */
    List<CheckListCellModel> getModel() {
        return model;
    }

}

