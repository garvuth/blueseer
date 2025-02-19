/*
The MIT License (MIT)

Copyright (c) Terry Evans Vaughn 

All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.blueseer.vdr;

import bsmf.MainFrame;
import static bsmf.MainFrame.db;
import static bsmf.MainFrame.ds;
import static bsmf.MainFrame.pass;
import com.blueseer.utl.OVData;
import static bsmf.MainFrame.tags;
import static bsmf.MainFrame.url;
import static bsmf.MainFrame.user;
import com.blueseer.ctr.cusData;
import com.blueseer.fgl.fglData;
import com.blueseer.utl.BlueSeerUtils;
import static com.blueseer.utl.BlueSeerUtils.callDialog;
import static com.blueseer.utl.BlueSeerUtils.checkLength;
import com.blueseer.utl.BlueSeerUtils.dbaction;
import static com.blueseer.utl.BlueSeerUtils.getClassLabelTag;
import static com.blueseer.utl.BlueSeerUtils.getGlobalColumnTag;
import static com.blueseer.utl.BlueSeerUtils.getMessageTag;
import static com.blueseer.utl.BlueSeerUtils.luModel;
import static com.blueseer.utl.BlueSeerUtils.luTable;
import static com.blueseer.utl.BlueSeerUtils.lual;
import static com.blueseer.utl.BlueSeerUtils.ludialog;
import static com.blueseer.utl.BlueSeerUtils.luinput;
import static com.blueseer.utl.BlueSeerUtils.luml;
import static com.blueseer.utl.BlueSeerUtils.lurb1;
import static com.blueseer.utl.BlueSeerUtils.lurb2;
import com.blueseer.utl.DTData;
import com.blueseer.utl.IBlueSeer;
import com.blueseer.utl.IBlueSeerT;
import static com.blueseer.vdr.venData.addVDSDet;
import static com.blueseer.vdr.venData.addVendMstr;
import static com.blueseer.vdr.venData.deleteVendMstr;
import static com.blueseer.vdr.venData.getVDSDet;
import static com.blueseer.vdr.venData.getVendMstr;
import static com.blueseer.vdr.venData.updateVDSDet;
import static com.blueseer.vdr.venData.updateVendMstr;
import com.blueseer.vdr.venData.vd_mstr;
import com.blueseer.vdr.venData.vds_det;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingWorker;

/**
 *
 * @author vaughnte
 */
public class VendMaint extends javax.swing.JPanel implements IBlueSeerT {

     
    // global variable declarations
                boolean isLoad = false;
                public static vd_mstr k = null;
                public static String vdtype = "";
                
   // global datatablemodel declarations  
    javax.swing.table.DefaultTableModel contactmodel = new javax.swing.table.DefaultTableModel(new Object[][]{},
            new String[]{
                getGlobalColumnTag("id"), 
                getGlobalColumnTag("type"), 
                getGlobalColumnTag("name"), 
                getGlobalColumnTag("phone"), 
                getGlobalColumnTag("fax"), 
                getGlobalColumnTag("email")
            });
    javax.swing.table.DefaultTableModel attachmentmodel = new javax.swing.table.DefaultTableModel(new Object[][]{},
                        new String[]{getGlobalColumnTag("select"), 
                getGlobalColumnTag("file")})
            {
              @Override  
              public Class getColumnClass(int col) {  
                if (col == 0)       
                    return ImageIcon.class; 
                else return String.class;  //other columns accept String values  
              }  
            };
    
    
    /**
     * Creates new form VendMaintPanel
     */
    public VendMaint() {
        initComponents();
        setLanguageTags(this);
    }

    
                  
        // interface functions implemented
    public void executeTask(dbaction x, String[] y) { 
      
        class Task extends SwingWorker<String[], Void> {
       
          String type = "";
          String[] key = null;
          
          public Task(dbaction type, String[] key) { 
              this.type = type.name();
              this.key = key;
          } 
           
        @Override
        public String[] doInBackground() throws Exception {
            String[] message = new String[2];
            message[0] = "";
            message[1] = "";
            
            
             switch(this.type) {
                case "add":
                    message = addRecord(key);
                    break;
                case "update":
                    message = updateRecord(key);
                    break;
                case "delete":
                    message = deleteRecord(key);    
                    break;
                case "get":
                    message = getRecord(key);    
                    break;    
                default:
                    message = new String[]{"1", "unknown action"};
            }
            
            return message;
        }
 
        
       public void done() {
            try {
            String[] message = get();
           
            BlueSeerUtils.endTask(message);
           if (this.type.equals("delete")) {
             initvars(null);  
           } else if (this.type.equals("get")) {
             updateForm();  
             tbkey.requestFocus();
           } else {
             initvars(null);  
           }
           
            
            } catch (Exception e) {
                MainFrame.bslog(e);
            } 
           
        }
    }  
      
       BlueSeerUtils.startTask(new String[]{"","Running..."});
       Task z = new Task(x, y); 
       z.execute(); 
       
    }
   
    public void setPanelComponentState(Object myobj, boolean b) {
        JPanel panel = null;
        JTabbedPane tabpane = null;
        JScrollPane scrollpane = null;
        if (myobj instanceof JPanel) {
            panel = (JPanel) myobj;
        } else if (myobj instanceof JTabbedPane) {
           tabpane = (JTabbedPane) myobj; 
        } else if (myobj instanceof JScrollPane) {
           scrollpane = (JScrollPane) myobj;    
        } else {
            return;
        }
        
        if (panel != null) {
        panel.setEnabled(b);
        Component[] components = panel.getComponents();
        
            for (Component component : components) {
                if (component instanceof JLabel || component instanceof JTable ) {
                    continue;
                }
                if (component instanceof JPanel) {
                    setPanelComponentState((JPanel) component, b);
                }
                if (component instanceof JTabbedPane) {
                    setPanelComponentState((JTabbedPane) component, b);
                }
                if (component instanceof JScrollPane) {
                    setPanelComponentState((JScrollPane) component, b);
                }
                
                component.setEnabled(b);
            }
        }
            if (tabpane != null) {
                tabpane.setEnabled(b);
                Component[] componentspane = tabpane.getComponents();
                for (Component component : componentspane) {
                    if (component instanceof JLabel || component instanceof JTable ) {
                        continue;
                    }
                    if (component instanceof JPanel) {
                        setPanelComponentState((JPanel) component, b);
                    }
                    
                    component.setEnabled(b);
                    
                }
            }
            if (scrollpane != null) {
                scrollpane.setEnabled(b);
                JViewport viewport = scrollpane.getViewport();
                Component[] componentspane = viewport.getComponents();
                for (Component component : componentspane) {
                    if (component instanceof JLabel || component instanceof JTable ) {
                        continue;
                    }
                    component.setEnabled(b);
                }
            }
            
            overrideComponentState();
    } 
    
    public void setLanguageTags(Object myobj) {
       JPanel panel = null;
        JTabbedPane tabpane = null;
        JScrollPane scrollpane = null;
        if (myobj instanceof JPanel) {
            panel = (JPanel) myobj;
        } else if (myobj instanceof JTabbedPane) {
           tabpane = (JTabbedPane) myobj; 
        } else if (myobj instanceof JScrollPane) {
           scrollpane = (JScrollPane) myobj;    
        } else {
            return;
        }
       Component[] components = panel.getComponents();
       for (Component component : components) {
           if (component instanceof JPanel) {
                    if (tags.containsKey(this.getClass().getSimpleName() + ".panel." + component.getName())) {
                       ((JPanel) component).setBorder(BorderFactory.createTitledBorder(tags.getString(this.getClass().getSimpleName() +".panel." + component.getName())));
                    } 
                    setLanguageTags((JPanel) component);
                }
                if (component instanceof JLabel ) {
                    if (tags.containsKey(this.getClass().getSimpleName() + ".label." + component.getName())) {
                       ((JLabel) component).setText(tags.getString(this.getClass().getSimpleName() +".label." + component.getName()));
                    }
                }
                if (component instanceof JButton ) {
                    if (tags.containsKey("global.button." + component.getName())) {
                       ((JButton) component).setText(tags.getString("global.button." + component.getName()));
                    }
                }
                if (component instanceof JCheckBox) {
                    if (tags.containsKey(this.getClass().getSimpleName() + ".label." + component.getName())) {
                       ((JCheckBox) component).setText(tags.getString(this.getClass().getSimpleName() +".label." + component.getName()));
                    } 
                }
                if (component instanceof JRadioButton) {
                    if (tags.containsKey(this.getClass().getSimpleName() + ".label." + component.getName())) {
                       ((JRadioButton) component).setText(tags.getString(this.getClass().getSimpleName() +".label." + component.getName()));
                    } 
                }
       }
    }
        
    public void setComponentDefaultValues() {
       isLoad = true;
       
       jTabbedPane1.removeAll();
        jTabbedPane1.add("Main", mainPanel);
        jTabbedPane1.add("Locations", shiptoPanel);
        jTabbedPane1.add("Contact", contactPanel);
        jTabbedPane1.add("Attachments", panelAttachment);
       
        attachmentmodel.setNumRows(0);
        tableattachment.setModel(attachmentmodel);
        tableattachment.getTableHeader().setReorderingAllowed(false);
        tableattachment.getColumnModel().getColumn(0).setMaxWidth(100);
        
       java.util.Date now = new java.util.Date();
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
        tbdateadded.setText(dtf.format(now));
        tbdatemod.setText(dtf.format(now));
       
        tbkey.setText("");
        tbkey.setForeground(Color.black);
        tbkey.setEditable(true);
        tbname.setText("");
        tbline1.setText("");
        tbline2.setText("");
        tbline3.setText("");
        tbcity.setText("");
        tbzip.setText("");
        tbpricecode.setText("");
        tbmarket.setText("");
        tbdisccode.setText("");
        tbbuyer.setText("0");
        tbmainphone.setText("");
        tbmainemail.setText("");
        tbremarks.setText("");
       
        ddshiptype.setSelectedIndex(0);
        cb850.setSelected(false);
        
        tbsalesrep.setText("");
        tbgroup.setText("");
        tbmarket.setText("");
        tbbuyer.setText("");
        tbpricecode.setText("");
        tbdisccode.setText("");
        
       
        
        ddstate.removeAllItems();
        ArrayList states = OVData.getCodeMstrKeyList("state");
        for (int i = 0; i < states.size(); i++) {
            ddstate.addItem(states.get(i).toString());
            ddshipstate.addItem(states.get(i).toString());
        }
        if (ddstate.getItemCount() > 0) {
           ddstate.setSelectedIndex(0); 
        }
        if (ddshipstate.getItemCount() > 0) {
           ddshipstate.setSelectedIndex(0); 
        }
        
    
       if (ddcountry.getItemCount() == 0)
       for (int i = 0; i < OVData.countries.length; i++) {
            ddcountry.addItem(OVData.countries[i]);
            ddshipcountry.addItem(OVData.countries[i]);
        }
       
        ddsite.removeAllItems();
        ArrayList<String> sites = OVData.getSiteList();
        for (String code : sites) {
            ddsite.addItem(code);
        }
        ddsite.setSelectedItem(OVData.getDefaultSite());
       
       ddcarrier.removeAllItems();
        ArrayList myscac = OVData.getfreightlist();   
        for (int i = 0; i < myscac.size(); i++) {
            ddcarrier.addItem(myscac.get(i));
        }
        
       ddterms.removeAllItems();
        ArrayList custterms = cusData.gettermsmstrlist();
        for (int i = 0; i < custterms.size(); i++) {
            ddterms.addItem(custterms.get(i));
        }
        
        ddcurr.removeAllItems();
        ArrayList<String> curr = fglData.getCurrlist();
        for (int i = 0; i < curr.size(); i++) {
            ddcurr.addItem(curr.get(i));
        }
        ddcurr.setSelectedItem(OVData.getDefaultCurrency());
        
        ddbank.removeAllItems();
        ArrayList bank = OVData.getbanklist();
        for (int i = 0; i < bank.size(); i++) {
            ddbank.addItem(bank.get(i));
        }
        
         ddaccount.removeAllItems();
        ArrayList accounts = fglData.getGLAcctList();
        for (int i = 0; i < accounts.size(); i++) {
            ddaccount.addItem(accounts.get(i).toString());
        }
        ddaccount.setSelectedItem(OVData.getDefaultAPAcct());
        
        ddcc.removeAllItems();
        ArrayList ccs = fglData.getGLCCList();
        for (int i = 0; i < ccs.size(); i++) {
            ddcc.addItem(ccs.get(i).toString());
        }
        
        ddtaxcode.removeAllItems();
        ArrayList<String> taxcodes = OVData.gettaxcodelist();
        for (int i = 0; i < taxcodes.size(); i++) {
            ddtaxcode.addItem(taxcodes.get(i));
        }
        ddtaxcode.insertItemAt("", 0);
        ddtaxcode.setSelectedIndex(0);
       
        if (ddbank.getItemCount() > 0)
        ddbank.setSelectedIndex(0);
         if (ddcarrier.getItemCount() > 0)
        ddcarrier.setSelectedIndex(0);
        if (ddterms.getItemCount() > 0)
        ddterms.setSelectedIndex(0);
        if (ddcountry.getItemCount() > 0)
        ddcountry.setSelectedItem("USA");
        if (ddstate.getItemCount() > 0)
        ddstate.setSelectedIndex(0);
        
        
        
        
        // contacts
         tbcontactname.setText("");
        tbphone.setText("");
        tbfax.setText("");
        tbemail.setText("");
        
       isLoad = false;
    }
    
    public void newAction(String x) {
       setPanelComponentState(this, true);
        setComponentDefaultValues();
        BlueSeerUtils.message(new String[]{"0",BlueSeerUtils.addRecordInit});
        btupdate.setEnabled(false);
        btdelete.setEnabled(false);
        btnew.setEnabled(false);
        tbkey.setEditable(true);
        tbkey.setForeground(Color.blue);
        if (! x.isEmpty()) {
          tbkey.setText(String.valueOf(OVData.getNextNbr(x)));  
          tbkey.setEditable(false);
        } 
        tbkey.requestFocus();
    }
    
    public void setAction(String[] x) {
        String[] m = new String[2];
        if (x[0].equals("0")) { 
                   setPanelComponentState(this, true);
                   btadd.setEnabled(false);
                   tbkey.setEditable(false);
                   tbkey.setForeground(Color.blue);
        } else {
                   tbkey.setForeground(Color.red); 
        }
    }
     
    public boolean validateInput(dbaction x) {
       Map<String,Integer> f = OVData.getTableInfo("vd_mstr");
        int fc;

        fc = checkLength(f,"vd_addr");
        if (tbkey.getText().length() > fc || tbkey.getText().isEmpty()) {
            bsmf.MainFrame.show(getMessageTag(1032,"1" + "/" + fc));
            tbkey.requestFocus();
            return false;
        }  
        
         fc = checkLength(f,"vd_name");
        if (tbname.getText().length() > fc || tbname.getText().isEmpty()) {
            bsmf.MainFrame.show(getMessageTag(1032,"1" + "/" + fc));
            tbname.requestFocus();
            return false;
        } 
        
        fc = checkLength(f,"vd_line1");
        if (tbline1.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbline1.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_line2");
        if (tbline2.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbline2.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_line3");
        if (tbline3.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbline3.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_city");
        if (tbcity.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbcity.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_zip");
        if (tbzip.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbzip.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_phone");
        if (tbphone.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbphone.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_email");
        if (tbemail.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbemail.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_group");
        if (tbgroup.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbgroup.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_market");
        if (tbmarket.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbmarket.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_buyer");
        if (tbsalesrep.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbsalesrep.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vd_remarks");
        if (tbremarks.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbremarks.requestFocus();
            return false;
        }
        

        if ( ! OVData.isValidGLAcct(ddaccount.getSelectedItem().toString())) {
          bsmf.MainFrame.show(getMessageTag(1052));
          ddaccount.requestFocus();
          return false;
        }

        if ( ! OVData.isValidGLcc(ddcc.getSelectedItem().toString())) {
          bsmf.MainFrame.show(getMessageTag(1048));
          ddcc.requestFocus();
          return false;  
        }
                
               
        return true;
    }
    
    public void initvars(String[] arg) {
       
       setPanelComponentState(mainPanel, false); 
       setPanelComponentState(shiptoPanel, false); 
       setPanelComponentState(contactPanel, false);  
       setPanelComponentState(this, false); 
       setComponentDefaultValues();
        btnew.setEnabled(true);
        btlookup.setEnabled(true);
      
        
        if (arg != null && arg.length > 0) {
            executeTask(dbaction.get,arg);
        } else {
            tbkey.setEnabled(true);
            tbkey.setEditable(true);
            tbkey.requestFocus();
        }
    }
   
    public String[] getRecord(String[] key) {
        vd_mstr z = getVendMstr(key);
        k = z;
        getAttachments(key[0]);
        return k.m();
     }
    
    public String[] addRecord(String[] key) {
        String[] m = addVendMstr(createRecord());
        return m;   
    }
    
    public String[] updateRecord(String[] key) {
        String[] m = updateVendMstr(createRecord());
        return m;   
    }
    
    public String[] deleteRecord(String[] key) {
       String[] m = new String[2];
        boolean proceed = bsmf.MainFrame.warn(getMessageTag(1004));
        if (proceed) {
        m = deleteVendMstr(createRecord());
        initvars(null);
        return m;   
        } else {
           m = new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.deleteRecordCanceled};  
        }
        return m;
    }
    
    public vd_mstr createRecord() { 
        vd_mstr x = new vd_mstr(null, 
                tbkey.getText().toString(),
                ddsite.getSelectedItem().toString(), // site
                tbname.getText(),
                tbline1.getText(),
                tbline2.getText(),
                tbline3.getText(),
                tbcity.getText(),
                (ddstate.getSelectedItem() == null) ? "" : ddstate.getSelectedItem().toString(),
                tbzip.getText(),
                (ddcountry.getSelectedItem() == null) ? "" : ddcountry.getSelectedItem().toString(),
                tbdateadded.getText(),
                tbdatemod.getText(),
                bsmf.MainFrame.userid,
                tbgroup.getText(),
                tbmarket.getText(),
                tbbuyer.getText(),
                ddterms.getSelectedItem().toString(),
                ddcarrier.getSelectedItem().toString(),
                tbpricecode.getText(),
                tbdisccode.getText(),
                ddtaxcode.getSelectedItem().toString(),
                ddaccount.getSelectedItem().toString(),
                ddcc.getSelectedItem().toString(),
                tbremarks.getText(),
                "", // freighttype
                ddbank.getSelectedItem().toString(),
                ddcurr.getSelectedItem().toString(),
                tbmisc.getText(), 
                tbmainphone.getText(),
                tbmainemail.getText(),
                String.valueOf(BlueSeerUtils.boolToInt(cb850.isSelected())),
                vdtype // type ...if added via carrier maintenance...this value will be 'carrier'...otherwise blank
                );
        return x;
    }
   
    public vds_det createVDSDet(boolean sameAs) { 
        // added sameAs boolean to distinguish intial customer creation from post customer creation
        venData.vds_det x = null;
        if (sameAs) {
        x = new venData.vds_det(null, 
                tbkey.getText(),
                tbkey.getText(),
                tbname.getText(),
                tbline1.getText(),
                tbline2.getText(),
                tbline3.getText(),
                tbcity.getText(),
                (ddstate.getSelectedItem() == null) ? "" : ddstate.getSelectedItem().toString(),
                tbzip.getText(),
                (ddcountry.getSelectedItem() == null) ? "" : ddcountry.getSelectedItem().toString(),
                ddshiptype.getSelectedItem().toString() // type
                );
        } else {
        x = new venData.vds_det(null, 
                tbkey.getText(),
                tbshipcode.getText(),
                tbshipname.getText(),
                tbshipline1.getText(),
                tbshipline2.getText(),
                tbshipline3.getText(),
                tbshipcity.getText(),
                (ddshipstate.getSelectedItem() == null) ? "" : ddshipstate.getSelectedItem().toString(),
                tbshipzip.getText(),
                (ddshipcountry.getSelectedItem() == null) ? "" : ddshipcountry.getSelectedItem().toString(),
                ddshiptype.getSelectedItem().toString() // type
                );
        }
        return x;
    }
    
    
    public void lookUpFrame(String option) {
        
        luinput.removeActionListener(lual);
        lual = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
        if (option.equals("shipto")) {
             if (lurb1.isSelected()) {  
             luModel = DTData.getVendShipToBrowseUtil(luinput.getText(),0, "vds_type", tbkey.getText());
            } else if (lurb2.isSelected()) {
             luModel = DTData.getVendShipToBrowseUtil(luinput.getText(),0, "vds_name", tbkey.getText()); 
            } else {
             luModel = DTData.getVendShipToBrowseUtil(luinput.getText(),0, "vds_zip", tbkey.getText());   
            }
        } else {
            if (lurb1.isSelected()) {  
             luModel = DTData.getVendBrowseUtil(luinput.getText(),0, "vd_addr");
            } else if (lurb2.isSelected()) {
             luModel = DTData.getVendBrowseUtil(luinput.getText(),0, "vd_name");   
            } else {
             luModel = DTData.getVendBrowseUtil(luinput.getText(),0, "vd_zip");   
            }
        }
        
        luTable.setModel(luModel);
        luTable.getColumnModel().getColumn(0).setMaxWidth(50);
        if (luModel.getRowCount() < 1) {
            ludialog.setTitle(getMessageTag(1001));
        } else {
            ludialog.setTitle(getMessageTag(1002, String.valueOf(luModel.getRowCount())));
        }
        }
        };
        luinput.addActionListener(lual);
        
        luTable.removeMouseListener(luml);
        luml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JTable target = (JTable)e.getSource();
                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();
                if ( column == 0) {
                ludialog.dispose();
                    if (option.equals("shipto")) {
                      getShipTo(new String[]{target.getValueAt(row,1).toString(), target.getValueAt(row,2).toString()});
                    } else {
                      initvars(new String[]{target.getValueAt(row,1).toString(), target.getValueAt(row,2).toString()});  
                    }
                }
            }
        };
        luTable.addMouseListener(luml);
      
        if (option.equals("shipto")) {
            callDialog(getClassLabelTag("lbltype", this.getClass().getSimpleName()), 
                getClassLabelTag("lblname", this.getClass().getSimpleName()),
                getClassLabelTag("lblzip", this.getClass().getSimpleName())); 
        } else {
           callDialog(getClassLabelTag("lblid", this.getClass().getSimpleName()), 
                getClassLabelTag("lblname", this.getClass().getSimpleName()),
                getClassLabelTag("lblzip", this.getClass().getSimpleName()));  
        }
        
        
    }

    public String[] updateForm() {
        tbkey.setText(k.vd_addr());
        ddsite.setSelectedItem(k.vd_site());
        tbname.setText(k.vd_name());
        tbline1.setText(k.vd_line1());
        tbline2.setText(k.vd_line2());
        tbline3.setText(k.vd_line3());
        tbcity.setText(k.vd_city());
        ddstate.setSelectedItem(k.vd_state());
       ddcountry.setSelectedItem(k.vd_country());
        if (k.vd_country().equals("US")) {
            ddcountry.setSelectedItem("USA");
        } 
        if (k.vd_country().equals("United States")) {
            ddcountry.setSelectedItem("USA");
        } 
         if (k.vd_country().equals("CA")) {
            ddcountry.setSelectedItem("Canada");
        } 
        tbzip.setText(k.vd_zip());

        tbdateadded.setText(k.vd_dateadd());
        tbdatemod.setText(k.vd_datemod());
        tbgroup.setText(k.vd_group());
        tbmarket.setText(k.vd_market());
        tbbuyer.setText(k.vd_buyer());
        ddcarrier.setSelectedItem(k.vd_shipvia());
        tbmisc.setText(k.vd_misc());
        ddterms.setSelectedItem(k.vd_terms());
        tbpricecode.setText(k.vd_price_code());
        tbdisccode.setText(k.vd_disc_code());
        ddtaxcode.setSelectedItem(k.vd_tax_code());
        ddaccount.setSelectedItem(k.vd_ap_acct());
        ddcc.setSelectedItem(k.vd_ap_cc());
        tbremarks.setText(k.vd_remarks());
        ddbank.setSelectedItem(k.vd_bank());
         ddcurr.setSelectedItem(k.vd_curr());
        tbmainphone.setText(k.vd_phone());
        tbmainemail.setText(k.vd_email());
        cb850.setSelected(BlueSeerUtils.ConvertStringToBool(k.vd_is850export()));
        vdtype = k.vd_type();
        refreshContactTable(k.vd_addr());
        setAction(k.m());
        clearShipTo();
        return k.m();  
    }
    
    public void getAttachments(String id) {
        attachmentmodel.setNumRows(0);
        ArrayList<String> list = OVData.getSysMetaData(id, this.getClass().getSimpleName(), "attachments");
        for (String file : list) {
        attachmentmodel.addRow(new Object[]{BlueSeerUtils.clickflag,  
                               file
            });
        }
    }
    
    
    // custom functions
    public boolean validateInputShipTo(dbaction action) {
        
        Map<String,Integer> f = OVData.getTableInfo("vds_det");
        int fc;

        fc = checkLength(f,"vds_shipto");
        if (tbshipcode.getText().length() > fc || tbshipcode.getText().isEmpty()) {
            bsmf.MainFrame.show(getMessageTag(1032,"1" + "/" + fc));
            tbshipcode.requestFocus();
            return false;
        }  
        
        fc = checkLength(f,"vds_name");
        if (tbshipname.getText().length() > fc || tbshipname.getText().isEmpty()) {
            bsmf.MainFrame.show(getMessageTag(1032,"1" + "/" + fc));
            tbshipname.requestFocus();
            return false;
        } 
        
        fc = checkLength(f,"vds_line1");
        if (tbshipline1.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbshipline1.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vds_line2");
        if (tbshipline2.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbshipline2.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vds_line3");
        if (tbshipline3.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbshipline3.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vds_city");
        if (tbshipcity.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbshipcity.requestFocus();
            return false;
        }
        
        fc = checkLength(f,"vds_zip");
        if (tbshipzip.getText().length() > fc) {
            bsmf.MainFrame.show(getMessageTag(1032,"0" + "/" + fc));
            tbshipzip.requestFocus();
            return false;
        }

      return true;
     }
    
    public void addShipTo() {
        String[] m = addVDSDet(createVDSDet(false));
        bsmf.MainFrame.show(m[1]);
    }
    
    public void updateShipTo() {
        String[] m = updateVDSDet(createVDSDet(false));
        bsmf.MainFrame.show(m[1]);
    }
    
    public void clearShipTo() {
       tbshipname.setText("");
       tbshipline1.setText("");
       tbshipline2.setText("");
       tbshipline3.setText("");
       tbshipcity.setText("");
       tbshipzip.setText("");
       tbshipcode.setText("");
       ddshiptype.setSelectedIndex(0);
      
        if (ddshipstate.getItemCount() > 0) {
           ddshipstate.setSelectedIndex(0); 
        }
       
       if (ddshipcountry.getItemCount() > 0) {
       ddshipcountry.setSelectedItem("USA");
       }
       
     }
    
    public String[] getShipTo(String[] x) {
        String[] m = new String[2];
        vds_det k = getVDSDet(x[0], x[1]);
        tbshipcode.setText(k.vds_shipto());
        tbshipname.setText(k.vds_name());
        tbshipline1.setText(k.vds_line1());
        tbshipline2.setText(k.vds_line2());
        tbshipline3.setText(k.vds_line3());
        tbshipcity.setText(k.vds_city());
        tbshipzip.setText(k.vds_zip());
        ddshipstate.setSelectedItem(k.vds_state());
        ddshipcountry.setSelectedItem(k.vds_country());
        ddshiptype.setSelectedItem(k.vds_type());
        if (k.m()[0].equals("0")) {
            btshipedit.setEnabled(true);
            btshipnew.setEnabled(true);
            btshipadd.setEnabled(false);
            tbshipcode.setEditable(false);
            m = new String[]{BlueSeerUtils.SuccessBit, BlueSeerUtils.getRecordSuccess};;
           } else {
            btshipedit.setEnabled(false);
            btshipnew.setEnabled(true);
            btshipadd.setEnabled(false);   
            m = new String[]{BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordError};
           }
     return m;
        
    }
     
    public void overrideComponentState() {
         tbdateadded.setEditable(false);
         tbdatemod.setEditable(false);
    }
    
    public void addContact(String vend) {
        try {

           Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                boolean proceed = true;
                int i = 0;

                if (proceed) {
                    st.executeUpdate("insert into vdc_det "
                        + "(vdc_code, vdc_type, vdc_name, vdc_phone, vdc_fax, "
                            + "vdc_email ) "
                            + " values ( " + "'" + vend + "'" + ","
                            + "'" + ddcontacttype.getSelectedItem().toString() + "'" + ","
                            + "'" + tbcontactname.getText().replace("'", "") + "'" + ","
                            + "'" + tbphone.getText().replace("'", "") + "'" + ","
                            + "'" + tbfax.getText().replace("'", "") + "'" + ","
                            + "'" + tbemail.getText().replace("'", "") + "'"                           
                            + ")"
                            + ";");
        
                   
                    BlueSeerUtils.message(new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.addRecordSuccess});
                    
                  
                    
                } // if proceed
            } catch (SQLException s) {
                MainFrame.bslog(s);
                BlueSeerUtils.message(new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.addRecordSQLError});
            } finally {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
                con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
    }
    
    public void editContact(String vend, String z) {
        try {

           Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                boolean proceed = true;
                int i = 0;

                if (proceed) {
                    st.executeUpdate("update vdc_det set "
                            + "vdc_type = " + "'" + ddcontacttype.getSelectedItem().toString() + "'" + ","
                            + "vdc_name = " + "'" + tbcontactname.getText().replace("'", "") + "'" + ","
                            + "vdc_phone = " + "'" + tbphone.getText().replace("'", "") + "'" + ","
                            + "vdc_fax = " +  "'" + tbfax.getText().replace("'", "") + "'" + ","
                            + "vdc_email = " + "'" + tbemail.getText().replace("'", "") + "'"                           
                            + " where vdc_code = " + "'" + vend + "'"
                            + " and vdc_id = " + "'" + z + "'" 
                            + ";");
        
                   
                   BlueSeerUtils.message(new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.updateRecordSuccess});
                    
                  
                    
                } // if proceed
            } catch (SQLException s) {
                MainFrame.bslog(s);
                BlueSeerUtils.message(new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.updateRecordSQLError});
            } finally {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
                con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
    }
    
    public void deleteContact(String vend, String z) {
        try {

           Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                boolean proceed = true;
                int i = 0;

                if (proceed) {
                    st.executeUpdate("delete from vdc_det where vdc_id = " + "'" + z + "'"
                            + " AND vdc_code = " + "'" + vend + "'"
                            + ";");
                    BlueSeerUtils.message(new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.deleteRecordSuccess});
                } // if proceed
            } catch (SQLException s) {
                MainFrame.bslog(s);
                BlueSeerUtils.message(new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.deleteRecordSQLError});
            } finally {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
                con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
    }
   
    public void clearAllContacts() {
         tbcontactname.setText("");
        tbphone.setText("");
        tbfax.setText("");
        tbemail.setText("");
    }
            
    public void refreshContactTable(String vend) {
      contactmodel.setRowCount(0);
       try {
            
            Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                res = st.executeQuery("select * from vdc_det where vdc_code = " + "'" + vend + "'" + ";");
                while (res.next()) {
                    contactmodel.addRow(new Object[]{res.getString("vdc_id"), res.getString("vdc_type"), res.getString("vdc_name"), res.getString("vdc_phone"), res.getString("vdc_fax"), res.getString("vdc_email") }); 
                }
                contacttable.setModel(contactmodel);
                
                 } catch (SQLException s) {
                MainFrame.bslog(s);
                BlueSeerUtils.message(new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordSQLError});
            } finally {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
                con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
       
     }
    
    
   
     
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        mainPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        tbcity = new javax.swing.JTextField();
        tbzip = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ddcountry = new javax.swing.JComboBox();
        ddstate = new javax.swing.JComboBox();
        tbline2 = new javax.swing.JTextField();
        tbline3 = new javax.swing.JTextField();
        tbname = new javax.swing.JTextField();
        tbline1 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        tbkey = new javax.swing.JTextField();
        btnew = new javax.swing.JButton();
        tbmainphone = new javax.swing.JTextField();
        tbmainemail = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btclear = new javax.swing.JButton();
        btlookup = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        tbdatemod = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        tbmarket = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        tbdateadded = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        tbgroup = new javax.swing.JTextField();
        tbbuyer = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        ddterms = new javax.swing.JComboBox();
        jLabel15 = new javax.swing.JLabel();
        ddcarrier = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        tbdisccode = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        tbpricecode = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        tbsalesrep = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbremarks = new javax.swing.JTextArea();
        ddbank = new javax.swing.JComboBox();
        jLabel30 = new javax.swing.JLabel();
        ddaccount = new javax.swing.JComboBox<>();
        ddcc = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        tbmisc = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        btadd = new javax.swing.JButton();
        btupdate = new javax.swing.JButton();
        btdelete = new javax.swing.JButton();
        ddcurr = new javax.swing.JComboBox<>();
        jLabel33 = new javax.swing.JLabel();
        cb850 = new javax.swing.JCheckBox();
        ddtaxcode = new javax.swing.JComboBox<>();
        ddsite = new javax.swing.JComboBox<>();
        jLabel44 = new javax.swing.JLabel();
        contactPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        contacttable = new javax.swing.JTable();
        btDeleteContact = new javax.swing.JButton();
        tbcontactname = new javax.swing.JTextField();
        btAddContact = new javax.swing.JButton();
        jLabel24 = new javax.swing.JLabel();
        tbemail = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        tbphone = new javax.swing.JTextField();
        ddcontacttype = new javax.swing.JComboBox();
        jLabel23 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        tbfax = new javax.swing.JTextField();
        btEditContact = new javax.swing.JButton();
        shiptoPanel = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        tbshipcity = new javax.swing.JTextField();
        tbshipzip = new javax.swing.JTextField();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        ddshipcountry = new javax.swing.JComboBox();
        ddshipstate = new javax.swing.JComboBox();
        tbshipline2 = new javax.swing.JTextField();
        tbshipline3 = new javax.swing.JTextField();
        tbshipname = new javax.swing.JTextField();
        tbshipline1 = new javax.swing.JTextField();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        btshipadd = new javax.swing.JButton();
        btshipedit = new javax.swing.JButton();
        btshipnew = new javax.swing.JButton();
        tbshipcode = new javax.swing.JTextField();
        btlookupShipTo = new javax.swing.JButton();
        ddshiptype = new javax.swing.JComboBox<>();
        jLabel43 = new javax.swing.JLabel();
        panelAttachment = new javax.swing.JPanel();
        labelmessage = new javax.swing.JLabel();
        btaddattachment = new javax.swing.JButton();
        btdeleteattachment = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableattachment = new javax.swing.JTable();

        jTextField1.setText("jTextField1");

        setBackground(new java.awt.Color(0, 102, 204));
        add(jTabbedPane1);

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Vendor Master Maintenance"));
        mainPanel.setName("panelmain"); // NOI18N

        jLabel7.setText("State");
        jLabel7.setName("lblstate"); // NOI18N

        jLabel5.setText("Line3");
        jLabel5.setName("lbladdr3"); // NOI18N

        jLabel8.setText("Zip/Post Code");
        jLabel8.setName("lblzip"); // NOI18N

        jLabel3.setText("Line1");
        jLabel3.setName("lbladdr1"); // NOI18N

        jLabel9.setText("Country");
        jLabel9.setName("lblcountry"); // NOI18N

        jLabel4.setText("Line2");
        jLabel4.setName("lbladdr2"); // NOI18N

        jLabel1.setText("VendCode");
        jLabel1.setName("lblid"); // NOI18N

        jLabel2.setText("Name");
        jLabel2.setName("lblname"); // NOI18N

        jLabel6.setText("City");
        jLabel6.setName("lblcity"); // NOI18N

        tbkey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbkeyActionPerformed(evt);
            }
        });

        btnew.setText("New");
        btnew.setName("btnew"); // NOI18N
        btnew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnewActionPerformed(evt);
            }
        });

        jLabel17.setText("Phone");
        jLabel17.setName("lblphone"); // NOI18N

        jLabel19.setText("Email");
        jLabel19.setName("lblemail"); // NOI18N

        btclear.setText("Clear");
        btclear.setName("btclear"); // NOI18N
        btclear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btclearActionPerformed(evt);
            }
        });

        btlookup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/lookup.png"))); // NOI18N
        btlookup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btlookupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addComponent(jLabel17, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbline3, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbline2, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbline1, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ddstate, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbcity, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbzip, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbname, javax.swing.GroupLayout.PREFERRED_SIZE, 196, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btlookup, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(btnew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btclear))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tbmainphone)
                            .addComponent(ddcountry, 0, 254, Short.MAX_VALUE)
                            .addComponent(tbmainemail))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnew)
                        .addComponent(btclear))
                    .addComponent(btlookup))
                .addGap(16, 16, 16)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbline1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbline2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbline3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbcity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddstate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(11, 11, 11)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbzip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddcountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbmainphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbmainemail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel10.setText("DateAdd");
        jLabel10.setName("lbldateadded"); // NOI18N

        jLabel13.setText("Group");
        jLabel13.setName("lblgroup"); // NOI18N

        jLabel12.setText("Market");
        jLabel12.setName("lblmarket"); // NOI18N

        jLabel11.setText("LastMod");
        jLabel11.setName("lbllastmod"); // NOI18N

        jLabel14.setText("Buyer");
        jLabel14.setName("lblbuyer"); // NOI18N

        jLabel15.setText("Terms");
        jLabel15.setName("lblterms"); // NOI18N

        jLabel16.setText("Carrier");
        jLabel16.setName("lblcarrier"); // NOI18N

        jLabel18.setText("Disc Code");
        jLabel18.setName("lbldisccode"); // NOI18N

        jLabel20.setText("Tax Code");
        jLabel20.setName("lbltaxcode"); // NOI18N

        jLabel26.setText("SalesRep");
        jLabel26.setName("lblsalesrep"); // NOI18N

        jLabel27.setText("AP Account");
        jLabel27.setName("lbacct"); // NOI18N

        jLabel28.setText("CostCenter");
        jLabel28.setName("lblcc"); // NOI18N

        jLabel29.setText("Remarks");
        jLabel29.setName("lblremarks"); // NOI18N

        tbremarks.setColumns(20);
        tbremarks.setRows(5);
        jScrollPane2.setViewportView(tbremarks);

        jLabel30.setText("Bank");
        jLabel30.setName("lblbank"); // NOI18N

        jLabel31.setText("Price Code");
        jLabel31.setName("lblpricecode"); // NOI18N

        jLabel32.setText("Misc");
        jLabel32.setName("lblmisc"); // NOI18N

        btadd.setText("Add");
        btadd.setName("btadd"); // NOI18N
        btadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btaddActionPerformed(evt);
            }
        });

        btupdate.setText("Update");
        btupdate.setName("btupdate"); // NOI18N
        btupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btupdateActionPerformed(evt);
            }
        });

        btdelete.setText("Delete");
        btdelete.setName("btdelete"); // NOI18N
        btdelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btdeleteActionPerformed(evt);
            }
        });

        jLabel33.setText("Currency");
        jLabel33.setName("lblcurrency"); // NOI18N

        cb850.setText("Export 850");

        jLabel44.setText("Site");
        jLabel44.setName("lblsite"); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btdelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btupdate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btadd))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel29)
                            .addComponent(jLabel13)
                            .addComponent(jLabel30)
                            .addComponent(jLabel31)
                            .addComponent(jLabel26)
                            .addComponent(jLabel27)
                            .addComponent(jLabel32)
                            .addComponent(jLabel10)
                            .addComponent(jLabel16)
                            .addComponent(jLabel33)
                            .addComponent(jLabel44))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(ddcurr, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tbpricecode, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tbsalesrep, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ddaccount, 0, 120, Short.MAX_VALUE)
                                    .addComponent(tbmisc, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tbdateadded, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tbgroup, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ddbank, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(ddcarrier, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(68, 68, 68)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel28, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tbdatemod)
                                    .addComponent(tbmarket)
                                    .addComponent(tbbuyer)
                                    .addComponent(tbdisccode)
                                    .addComponent(ddterms, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(ddcc, 0, 97, Short.MAX_VALUE)
                                    .addComponent(cb850)
                                    .addComponent(ddtaxcode, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(ddsite, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addGap(62, 62, 62))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddsite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(tbdatemod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(tbdateadded, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(tbmarket, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
                        .addComponent(tbbuyer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addGap(19, 19, 19)
                        .addComponent(jLabel14))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(tbgroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(13, 13, 13)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel30)
                            .addComponent(ddbank, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddcurr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel33)
                    .addComponent(cb850))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddcarrier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(ddterms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbpricecode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel31)
                    .addComponent(tbdisccode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addGap(3, 3, 3)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel20)
                            .addComponent(ddtaxcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(ddcc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbsalesrep, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ddaccount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbmisc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel32))))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel29)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btadd)
                    .addComponent(btupdate)
                    .addComponent(btdelete))
                .addGap(7, 7, 7))
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        add(mainPanel);

        contactPanel.setPreferredSize(new java.awt.Dimension(938, 421));

        contacttable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Type", "Name", "Phone", "Fax", "Email"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        contacttable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                contacttableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(contacttable);

        btDeleteContact.setText("DeleteContact");
        btDeleteContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeleteContactActionPerformed(evt);
            }
        });

        btAddContact.setText("AddContact");
        btAddContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAddContactActionPerformed(evt);
            }
        });

        jLabel24.setText("ContactType");
        jLabel24.setName("lbltype"); // NOI18N

        jLabel21.setText("ContactName");
        jLabel21.setName("lblname"); // NOI18N

        ddcontacttype.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sales", "Finance", "IT", "Admin", "Shipping", "Engineering", "Quality" }));

        jLabel23.setText("Email");
        jLabel23.setName("lblemail"); // NOI18N

        jLabel22.setText("Phone");
        jLabel22.setName("lblphone"); // NOI18N

        jLabel25.setText("Fax");
        jLabel25.setName("lblfax"); // NOI18N

        btEditContact.setText("EditContact");
        btEditContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditContactActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout contactPanelLayout = new javax.swing.GroupLayout(contactPanel);
        contactPanel.setLayout(contactPanelLayout);
        contactPanelLayout.setHorizontalGroup(
            contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contactPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(contactPanelLayout.createSequentialGroup()
                        .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel21)
                            .addComponent(jLabel24))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(contactPanelLayout.createSequentialGroup()
                                .addComponent(ddcontacttype, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tbphone, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(tbcontactname))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel25, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(contactPanelLayout.createSequentialGroup()
                                .addComponent(tbfax, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btEditContact)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btAddContact)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btDeleteContact))
                            .addComponent(tbemail, javax.swing.GroupLayout.PREFERRED_SIZE, 440, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        contactPanelLayout.setVerticalGroup(
            contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(contactPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(tbphone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(ddcontacttype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(tbfax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btDeleteContact)
                    .addComponent(btAddContact)
                    .addComponent(btEditContact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(contactPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(tbcontactname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tbemail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(257, Short.MAX_VALUE))
        );

        add(contactPanel);

        shiptoPanel.setBackground(new java.awt.Color(220, 220, 220));

        jLabel34.setText("State");
        jLabel34.setName("lblstate"); // NOI18N

        jLabel35.setText("Line3");
        jLabel35.setName("lbladdr3"); // NOI18N

        jLabel36.setText("Zip");
        jLabel36.setName("lblzip"); // NOI18N

        jLabel37.setText("Line1");
        jLabel37.setName("lbladdr1"); // NOI18N

        jLabel38.setText("Country");
        jLabel38.setName("lblcountry"); // NOI18N

        jLabel39.setText("Line2");
        jLabel39.setName("lbladdr2"); // NOI18N

        jLabel40.setText("ShipCode");
        jLabel40.setName("lblshipto"); // NOI18N

        jLabel41.setText("Name");
        jLabel41.setName("lblname"); // NOI18N

        jLabel42.setText("City");
        jLabel42.setName("lblcity"); // NOI18N

        btshipadd.setText("Add");
        btshipadd.setName("btadd"); // NOI18N
        btshipadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btshipaddActionPerformed(evt);
            }
        });

        btshipedit.setText("Update");
        btshipedit.setName("btupdate"); // NOI18N
        btshipedit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btshipeditActionPerformed(evt);
            }
        });

        btshipnew.setText("New");
        btshipnew.setName("btnew"); // NOI18N
        btshipnew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btshipnewActionPerformed(evt);
            }
        });

        btlookupShipTo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        btlookupShipTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btlookupShipToActionPerformed(evt);
            }
        });

        ddshiptype.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ShipFrom", "ShipTo" }));

        jLabel43.setText("Type");

        javax.swing.GroupLayout shiptoPanelLayout = new javax.swing.GroupLayout(shiptoPanel);
        shiptoPanel.setLayout(shiptoPanelLayout);
        shiptoPanelLayout.setHorizontalGroup(
            shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shiptoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(shiptoPanelLayout.createSequentialGroup()
                        .addComponent(btshipedit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btshipadd))
                    .addGroup(shiptoPanelLayout.createSequentialGroup()
                        .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel40, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel43, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel41, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel37, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel39, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel42, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel34, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel36, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel38, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(shiptoPanelLayout.createSequentialGroup()
                                .addComponent(tbshipcode, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btlookupShipTo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btshipnew))
                            .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(tbshipline3, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                .addComponent(tbshipline2, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                .addComponent(tbshipline1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                .addComponent(tbshipcity, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                                .addComponent(tbshipname)
                                .addComponent(ddshipstate, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(ddshipcountry, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbshipzip, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(ddshiptype, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(37, 37, 37)))
                .addContainerGap(522, Short.MAX_VALUE))
        );
        shiptoPanelLayout.setVerticalGroup(
            shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(shiptoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btshipnew)
                    .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbshipcode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel40))
                    .addComponent(btlookupShipTo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddshiptype, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel43))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel41))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipline1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel37))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipline2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipline3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel35))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipcity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel42))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddshipstate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbshipzip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel36))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ddshipcountry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel38))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(shiptoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btshipadd)
                    .addComponent(btshipedit))
                .addContainerGap(219, Short.MAX_VALUE))
        );

        add(shiptoPanel);

        panelAttachment.setBorder(javax.swing.BorderFactory.createTitledBorder("Attachment Panel"));
        panelAttachment.setName("panelAttachment"); // NOI18N
        panelAttachment.setPreferredSize(new java.awt.Dimension(974, 560));

        btaddattachment.setText("Add Attachment");
        btaddattachment.setName("btaddattachment"); // NOI18N
        btaddattachment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btaddattachmentActionPerformed(evt);
            }
        });

        btdeleteattachment.setText("Delete Attachment");
        btdeleteattachment.setName("btdeleteattachment"); // NOI18N
        btdeleteattachment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btdeleteattachmentActionPerformed(evt);
            }
        });

        tableattachment.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableattachment.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableattachmentMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(tableattachment);

        javax.swing.GroupLayout panelAttachmentLayout = new javax.swing.GroupLayout(panelAttachment);
        panelAttachment.setLayout(panelAttachmentLayout);
        panelAttachmentLayout.setHorizontalGroup(
            panelAttachmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttachmentLayout.createSequentialGroup()
                .addGroup(panelAttachmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelAttachmentLayout.createSequentialGroup()
                        .addComponent(btaddattachment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btdeleteattachment)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 446, Short.MAX_VALUE)
                        .addComponent(labelmessage, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelAttachmentLayout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelAttachmentLayout.setVerticalGroup(
            panelAttachmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelAttachmentLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelAttachmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelmessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(panelAttachmentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btaddattachment)
                        .addComponent(btdeleteattachment)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(157, 157, 157))
        );

        add(panelAttachment);
    }// </editor-fold>//GEN-END:initComponents

    private void btaddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btaddActionPerformed
          if (! validateInput(dbaction.add)) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask(dbaction.add, new String[]{tbkey.getText()});
    }//GEN-LAST:event_btaddActionPerformed

    private void btAddContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAddContactActionPerformed
                         addContact(tbkey.getText());
                         refreshContactTable(tbkey.getText());
    }//GEN-LAST:event_btAddContactActionPerformed

    private void btDeleteContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeleteContactActionPerformed
          int[] rows = contacttable.getSelectedRows();
        for (int i : rows) {
           deleteContact(tbkey.getText(), contacttable.getValueAt(i, 0).toString());
        }
       refreshContactTable(tbkey.getText());
       clearAllContacts();
    }//GEN-LAST:event_btDeleteContactActionPerformed

    private void btupdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btupdateActionPerformed
        if (! validateInput(dbaction.update)) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask(dbaction.update, new String[]{tbkey.getText()});
       
    }//GEN-LAST:event_btupdateActionPerformed

    private void btnewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnewActionPerformed
        if (OVData.isAutoVend()) {
          newAction("vendor");
        } else {
           newAction("");
           tbkey.requestFocus();
         }
      
    }//GEN-LAST:event_btnewActionPerformed

    private void btEditContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditContactActionPerformed
        int[] rows = contacttable.getSelectedRows();
        for (int i : rows) {
            editContact(tbkey.getText(), contacttable.getValueAt(i, 0).toString());
        }
        refreshContactTable(tbkey.getText());
        clearAllContacts();
    }//GEN-LAST:event_btEditContactActionPerformed

    private void contacttableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_contacttableMouseClicked
        int row = contacttable.rowAtPoint(evt.getPoint());
        ddcontacttype.setSelectedItem(contacttable.getValueAt(row, 1).toString());
        tbcontactname.setText(contacttable.getValueAt(row, 2).toString());
        tbphone.setText(contacttable.getValueAt(row, 3).toString());
        tbfax.setText(contacttable.getValueAt(row, 4).toString());
        tbemail.setText(contacttable.getValueAt(row, 5).toString());
    }//GEN-LAST:event_contacttableMouseClicked

    private void btdeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btdeleteActionPerformed
          if (! validateInput(dbaction.delete)) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask(dbaction.delete, new String[]{tbkey.getText()});  
    }//GEN-LAST:event_btdeleteActionPerformed

    private void tbkeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbkeyActionPerformed
        if (! btadd.isEnabled())
        executeTask(dbaction.get, new String[]{tbkey.getText()});
    }//GEN-LAST:event_tbkeyActionPerformed

    private void btclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btclearActionPerformed
        BlueSeerUtils.messagereset();
        initvars(null);
    }//GEN-LAST:event_btclearActionPerformed

    private void btlookupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btlookupActionPerformed
        lookUpFrame("");
    }//GEN-LAST:event_btlookupActionPerformed

    private void btshipaddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btshipaddActionPerformed
        if (! validateInputShipTo(dbaction.add)) {
            return;
        }
        if (OVData.isValidCustShipTo(tbkey.getText(),tbshipcode.getText())) {
            bsmf.MainFrame.show(getMessageTag(1014));
            return;
        }
        addShipTo();
    }//GEN-LAST:event_btshipaddActionPerformed

    private void btshipeditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btshipeditActionPerformed
        if (! validateInputShipTo(dbaction.update)) {
            return;
        }
        if (OVData.isValidVendAddr(tbkey.getText(),tbshipcode.getText())) {
            updateShipTo();
        }
    }//GEN-LAST:event_btshipeditActionPerformed

    private void btshipnewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btshipnewActionPerformed
        clearShipTo();
        if (OVData.isAutoCust()) {
            tbshipcode.setText(String.valueOf(OVData.getNextNbr("shipto")));
            tbshipcode.setEditable(false);
            btshipadd.setEnabled(true);
            btshipedit.setEnabled(false);
        } else {
            tbshipcode.setEditable(true);
            tbshipcode.requestFocus();
            btshipadd.setEnabled(true);
            btshipedit.setEnabled(false);

        }

    }//GEN-LAST:event_btshipnewActionPerformed

    private void btlookupShipToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btlookupShipToActionPerformed
        lookUpFrame("shipto");
    }//GEN-LAST:event_btlookupShipToActionPerformed

    private void btaddattachmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btaddattachmentActionPerformed
        OVData.addFileAttachment(tbkey.getText(), this.getClass().getSimpleName(), this );
        getAttachments(tbkey.getText());
    }//GEN-LAST:event_btaddattachmentActionPerformed

    private void btdeleteattachmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btdeleteattachmentActionPerformed
        boolean proceed = bsmf.MainFrame.warn(getMessageTag(1004));
        if (proceed) {
            int[] rows = tableattachment.getSelectedRows();
            String filename = null;
            for (int i : rows) {
                filename = tableattachment.getValueAt(i, 1).toString();
            }
            OVData.deleteFileAttachment(tbkey.getText(),this.getClass().getSimpleName(),filename);
            getAttachments(tbkey.getText());
        }
    }//GEN-LAST:event_btdeleteattachmentActionPerformed

    private void tableattachmentMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableattachmentMouseClicked
        int row = tableattachment.rowAtPoint(evt.getPoint());
        int col = tableattachment.columnAtPoint(evt.getPoint());
        if ( col == 0) {
            OVData.openFileAttachment(tbkey.getText(), this.getClass().getSimpleName(), tableattachment.getValueAt(row, 1).toString());
        }
    }//GEN-LAST:event_tableattachmentMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btAddContact;
    private javax.swing.JButton btDeleteContact;
    private javax.swing.JButton btEditContact;
    private javax.swing.JButton btadd;
    private javax.swing.JButton btaddattachment;
    private javax.swing.JButton btclear;
    private javax.swing.JButton btdelete;
    private javax.swing.JButton btdeleteattachment;
    private javax.swing.JButton btlookup;
    private javax.swing.JButton btlookupShipTo;
    private javax.swing.JButton btnew;
    private javax.swing.JButton btshipadd;
    private javax.swing.JButton btshipedit;
    private javax.swing.JButton btshipnew;
    private javax.swing.JButton btupdate;
    private javax.swing.JCheckBox cb850;
    private javax.swing.JPanel contactPanel;
    private javax.swing.JTable contacttable;
    private javax.swing.JComboBox<String> ddaccount;
    private javax.swing.JComboBox ddbank;
    private javax.swing.JComboBox ddcarrier;
    private javax.swing.JComboBox<String> ddcc;
    private javax.swing.JComboBox ddcontacttype;
    private javax.swing.JComboBox ddcountry;
    private javax.swing.JComboBox<String> ddcurr;
    private javax.swing.JComboBox ddshipcountry;
    private javax.swing.JComboBox ddshipstate;
    private javax.swing.JComboBox<String> ddshiptype;
    private javax.swing.JComboBox<String> ddsite;
    private javax.swing.JComboBox ddstate;
    private javax.swing.JComboBox<String> ddtaxcode;
    private javax.swing.JComboBox ddterms;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel labelmessage;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel panelAttachment;
    private javax.swing.JPanel shiptoPanel;
    private javax.swing.JTable tableattachment;
    private javax.swing.JTextField tbbuyer;
    private javax.swing.JTextField tbcity;
    private javax.swing.JTextField tbcontactname;
    private javax.swing.JTextField tbdateadded;
    private javax.swing.JTextField tbdatemod;
    private javax.swing.JTextField tbdisccode;
    private javax.swing.JTextField tbemail;
    private javax.swing.JTextField tbfax;
    private javax.swing.JTextField tbgroup;
    private javax.swing.JTextField tbkey;
    private javax.swing.JTextField tbline1;
    private javax.swing.JTextField tbline2;
    private javax.swing.JTextField tbline3;
    private javax.swing.JTextField tbmainemail;
    private javax.swing.JTextField tbmainphone;
    private javax.swing.JTextField tbmarket;
    private javax.swing.JTextField tbmisc;
    private javax.swing.JTextField tbname;
    private javax.swing.JTextField tbphone;
    private javax.swing.JTextField tbpricecode;
    private javax.swing.JTextArea tbremarks;
    private javax.swing.JTextField tbsalesrep;
    private javax.swing.JTextField tbshipcity;
    private javax.swing.JTextField tbshipcode;
    private javax.swing.JTextField tbshipline1;
    private javax.swing.JTextField tbshipline2;
    private javax.swing.JTextField tbshipline3;
    private javax.swing.JTextField tbshipname;
    private javax.swing.JTextField tbshipzip;
    private javax.swing.JTextField tbzip;
    // End of variables declaration//GEN-END:variables
}
