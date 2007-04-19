// $Id: eclipse-argo-codetemplates.xml 11347 2006-10-26 22:37:44Z linus $
// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.argouml.i18n.Translator;
import org.argouml.language.sql.GeneratorSql;

public class SelectCodeCreatorDialog extends ArgoDialog {
    private JLabel lblSelect;

    private JScrollPane spList;

    private JList listCreators;

    private static boolean executed;

    public static boolean execute() {
        executed = false;

        SelectCodeCreatorDialog d = new SelectCodeCreatorDialog();
        d.setVisible(true);

        return executed;
    }

    public SelectCodeCreatorDialog() {
        super(Translator.localize("argouml-sql.select-dialog.title"),
                OK_CANCEL_OPTION, true);

        setPreferredSize(new Dimension(400, 300));

        GridBagLayout l = new GridBagLayout();
        l.rowWeights = new double[] { 0, 1 };
        l.columnWeights = new double[] { 1 };

        JPanel content = new JPanel();
        content.setLayout(l);

        lblSelect = new JLabel(Translator
                .localize("argouml-sql.select-dialog.label-select"));
        listCreators = new JList(new ListModelCodeCreators());
        spList = new JScrollPane(listCreators);

        content.add(lblSelect, GridBagUtils.captionConstraints(0, 0,
                GridBagUtils.left));
        content.add(spList, GridBagUtils.clientAlignConstraints(0, 1));
        setContent(content);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getOkButton()) {
            executed = true;
        } else {
            executed = false;
        }
        dispose();
    }
}
