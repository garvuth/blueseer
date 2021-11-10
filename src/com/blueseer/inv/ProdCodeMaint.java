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

package com.blueseer.inv;

import bsmf.MainFrame;
import static bsmf.MainFrame.db;
import static bsmf.MainFrame.pass;
import com.blueseer.utl.OVData;
import static bsmf.MainFrame.reinitpanels;
import static bsmf.MainFrame.tags;
import static bsmf.MainFrame.url;
import static bsmf.MainFrame.user;
import com.blueseer.utl.BlueSeerUtils;
import static com.blueseer.utl.BlueSeerUtils.callDialog;
import static com.blueseer.utl.BlueSeerUtils.getClassLabelTag;
import static com.blueseer.utl.BlueSeerUtils.getMessageTag;
import static com.blueseer.utl.BlueSeerUtils.luModel;
import static com.blueseer.utl.BlueSeerUtils.luTable;
import static com.blueseer.utl.BlueSeerUtils.lual;
import static com.blueseer.utl.BlueSeerUtils.ludialog;
import static com.blueseer.utl.BlueSeerUtils.luinput;
import static com.blueseer.utl.BlueSeerUtils.luml;
import static com.blueseer.utl.BlueSeerUtils.lurb1;
import com.blueseer.utl.DTData;
import com.blueseer.utl.IBlueSeer;
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
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
public class ProdCodeMaint extends javax.swing.JPanel implements IBlueSeer {

   // global variable declarations
                boolean isLoad = false;
    
   // global datatablemodel declarations    s new form ProdCodeMaintPanel
     
                
                
    public ProdCodeMaint() {
        initComponents();
        setLanguageTags(this);
    }

    
    // interface functions implemented
    public void executeTask(String x, String[] y) { 
      
        class Task extends SwingWorker<String[], Void> {
       
          String type = "";
          String[] key = null;
          
          public Task(String type, String[] key) { 
              this.type = type;
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
           } else if (this.type.equals("get") && message[0].equals("1")) {
             tbkey.requestFocus();
           } else if (this.type.equals("get") && message[0].equals("0")) {
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
        tbkey.setText("");
         tbdesc.setText("");
         tbinvacct.setText("");
         tbinvdescrepancyacct.setText("");
         tbscrapacct.setText("");
         tbwipacct.setText("");
         tbwipvaracct.setText("");
         tbinvchangeacct.setText("");
         tbsalesacct.setText("");
         tbsalesdiscacct.setText("");
         tbcogsmtlacct.setText("");
         tbcogslbracct.setText("");
         tbcogsbdnacct.setText("");
         tbcogsovhacct.setText("");
         tbcogsoutacct.setText("");
         tbpurchacct.setText("");
         tbporcptacct.setText("");
         tbpoovhacct.setText("");
         tbpopricevaracct.setText("");
         tbapusageacct.setText("");
         tbapratevaracct.setText("");
         tbjobstockacct.setText("");
         tbmtlusagevaracct.setText("");
         tbmtlratevaracct.setText("");
         tbmixedvaracct.setText("");
         tbcopacct.setText("");
         tboutusgvaracct.setText("");
         tboutratevaracct.setText("");
         tbtbd.setText("");
        
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
    
    public String[] setAction(int i) {
        String[] m = new String[2];
        if (i > 0) {
            m = new String[]{BlueSeerUtils.SuccessBit, BlueSeerUtils.getRecordSuccess};  
                   setPanelComponentState(this, true);
                   btadd.setEnabled(false);
                   tbkey.setEditable(false);
                   tbkey.setForeground(Color.blue);
        } else {
           m = new String[]{BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordError};  
                   tbkey.setForeground(Color.red); 
        }
        return m;
    }
    
    public boolean validateInput(String x) {
        boolean b = true;
        
                        
        
                if (tbkey.getText().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1024));
                    tbkey.requestFocus();
                    return b;
                }
                
                if (tbdesc.getText().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1024));
                    tbdesc.requestFocus();
                    return b;
                }
                
                if (! OVData.isValidGLAcct(tbinvacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbinvacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbinvdescrepancyacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbinvdescrepancyacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbwipacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbwipacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbwipvaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbwipvaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbscrapacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbscrapacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbinvchangeacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbinvchangeacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbsalesacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbsalesacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbsalesdiscacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbsalesdiscacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcogsmtlacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcogsmtlacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcogslbracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcogslbracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcogsbdnacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcogsbdnacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcogsovhacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcogsovhacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcogsoutacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcogsoutacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbpurchacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbpurchacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbporcptacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbporcptacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbpoovhacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbpoovhacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbpopricevaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbpopricevaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbapusageacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbapusageacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbapratevaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbapratevaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbjobstockacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbjobstockacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbmtlusagevaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbmtlusagevaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbmtlratevaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbmtlratevaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbmixedvaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbmixedvaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tbcopacct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tbcopacct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tboutusgvaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tboutusgvaracct.requestFocus();
                   return b;
                }
         if (! OVData.isValidGLAcct(tboutratevaracct.getText().toString())) {
                   b = false;
                   bsmf.MainFrame.show(getMessageTag(1052));
                   tboutratevaracct.requestFocus();
                   return b;
                }
                
                
               
        return b;
    }
    
    public void initvars(String[] arg) {
       
       setPanelComponentState(this, false); 
       setComponentDefaultValues();
        btnew.setEnabled(true);
        btlookup.setEnabled(true);
        
        if (arg != null && arg.length > 0) {
            executeTask("get",arg);
        } else {
            tbkey.setEnabled(true);
            tbkey.setEditable(true);
            tbkey.requestFocus();
        }
    }
    
    public String[] addRecord(String[] x) {
     String[] m = new String[2];
     
     try {

            Connection con = DriverManager.getConnection(url + db, user, pass);
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                boolean proceed = true;
                int i = 0;
                
                proceed = validateInput("addRecord");
                
                if (proceed) {

                    res = st.executeQuery("SELECT pl_line FROM  pl_mstr where pl_line = " + "'" + tbkey.getText() + "'" + ";");
                    while (res.next()) {
                        i++;
                    }
                    if (i == 0) {
                        st.executeUpdate("insert into pl_mstr "
                            + "(pl_line, pl_desc, pl_inventory, pl_inv_discr, "
                            + "pl_scrap, pl_wip, pl_wip_var, pl_inv_change, pl_sales, pl_sales_disc, "
                            + "pl_cogs_mtl, pl_cogs_lbr, pl_cogs_bdn, pl_cogs_ovh, pl_cogs_out, "
                            + "pl_purchases, pl_po_rcpt, pl_po_ovh, pl_po_pricevar, pl_ap_usage, pl_ap_ratevar, "
                            + "pl_job_stock, pl_mtl_usagevar, pl_mtl_ratevar, pl_mix_var, pl_cop, pl_out_usagevar, pl_out_ratevar )"
                            + " values ( " + "'" + tbkey.getText().toString() + "'" + ","
                            + "'" + tbdesc.getText().toString() + "'" + ","
                            + "'" + tbinvacct.getText().toString() + "'" + ","
                            + "'" + tbinvdescrepancyacct.getText().toString() + "'" + ","
                            + "'" + tbscrapacct.getText().toString() + "'" + ","
                            + "'" + tbwipacct.getText().toString() + "'" + ","
                            + "'" + tbwipvaracct.getText().toString() + "'" + ","
                            + "'" + tbinvchangeacct.getText().toString() + "'" + ","
                            + "'" + tbsalesacct.getText().toString() + "'" + ","
                            + "'" + tbsalesdiscacct.getText().toString() + "'" + ","
                            + "'" + tbcogsmtlacct.getText().toString() + "'" + ","
                            + "'" + tbcogslbracct.getText().toString() + "'" + ","
                            + "'" + tbcogsbdnacct.getText().toString() + "'" + ","
                            + "'" + tbcogsovhacct.getText().toString() + "'" + ","
                            + "'" + tbcogsoutacct.getText().toString() + "'" + ","
                            + "'" + tbpurchacct.getText().toString() + "'" + ","
                            + "'" + tbporcptacct.getText().toString() + "'" + ","
                            + "'" + tbpoovhacct.getText().toString() + "'" + ","
                            + "'" + tbpopricevaracct.getText().toString() + "'" + ","
                            + "'" + tbapusageacct.getText().toString() + "'" + ","
                            + "'" + tbapratevaracct.getText().toString() + "'" + ","
                            + "'" + tbjobstockacct.getText().toString() + "'" + ","
                            + "'" + tbmtlusagevaracct.getText().toString() + "'" + ","
                            + "'" + tbmtlratevaracct.getText().toString() + "'" + ","
                            + "'" + tbmixedvaracct.getText().toString() + "'" + ","
                            + "'" + tbcopacct.getText().toString() + "'" + ","
                            + "'" + tboutusgvaracct.getText().toString() + "'" + ","
                            + "'" + tboutratevaracct.getText().toString() + "'"
                            + ")"
                            + ";");
                        m = new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.addRecordSuccess};
                    } else {
                       m = new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.addRecordAlreadyExists}; 
                    }

                   initvars(null);
                   
                } // if proceed
            } catch (SQLException s) {
                MainFrame.bslog(s);
                 m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName())};  
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
             m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1020, Thread.currentThread().getStackTrace()[1].getMethodName())};
        }
     
     return m;
     }
     
    public String[] updateRecord(String[] x) {
     String[] m = new String[2];
     
     try {
            boolean proceed = true;
            Connection con = DriverManager.getConnection(url + db, user, pass);
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
               proceed = validateInput("updateRecord");
                
                if (proceed) {
                    st.executeUpdate("update pl_mstr set "
                            + "pl_desc = " + "'" + tbdesc.getText().toString() + "'" + ","
                            + "pl_inventory = " + "'" + tbinvacct.getText().toString() + "'" + ","
                            + "pl_inv_discr = " + "'" + tbinvdescrepancyacct.getText().toString() + "'" + ","
                            + "pl_scrap = " + "'" + tbscrapacct.getText().toString() + "'" + ","
                            + "pl_wip = " + "'" + tbwipacct.getText().toString() + "'" + ","
                            + "pl_wip_var = " + "'" + tbwipvaracct.getText().toString() + "'" + ","
                            + "pl_inv_change = " + "'" + tbinvchangeacct.getText().toString() + "'" + ","
                            + "pl_sales = " + "'" + tbsalesacct.getText().toString() + "'" + ","
                            + "pl_sales_disc = " + "'" + tbsalesdiscacct.getText().toString() + "'" + ","
                            + "pl_cogs_mtl = " + "'" + tbcogsmtlacct.getText().toString() + "'" + ","
                            + "pl_cogs_lbr = " + "'" + tbcogslbracct.getText().toString() + "'" + ","
                            + "pl_cogs_bdn = " + "'" + tbcogsbdnacct.getText().toString() + "'" + ","
                            + "pl_cogs_ovh = " + "'" + tbcogsovhacct.getText().toString() + "'" + ","
                            + "pl_cogs_out = " + "'" + tbcogsoutacct.getText().toString() + "'" + ","
                            + "pl_purchases = " + "'" + tbpurchacct.getText().toString() + "'" + ","
                            + "pl_po_rcpt = " + "'" + tbporcptacct.getText().toString() + "'" + ","
                            + "pl_po_ovh = " + "'" + tbpoovhacct.getText().toString() + "'" + ","
                            + "pl_po_pricevar = " + "'" + tbpopricevaracct.getText().toString() + "'" + ","
                            + "pl_ap_usage = " + "'" + tbapusageacct.getText().toString() + "'" + ","
                            + "pl_ap_ratevar = " + "'" + tbapratevaracct.getText().toString() + "'" + ","
                            + "pl_job_stock = " + "'" + tbjobstockacct.getText().toString() + "'" + ","
                            + "pl_mtl_usagevar = " + "'" + tbmtlusagevaracct.getText().toString() + "'" + ","
                            + "pl_mtl_ratevar = " + "'" + tbmtlratevaracct.getText().toString() + "'" + ","
                            + "pl_mix_var = " + "'" + tbmixedvaracct.getText().toString() + "'" + ","
                            + "pl_cop = " + "'" + tbcopacct.getText().toString() + "'" + ","
                            + "pl_out_usagevar = " + "'" + tboutusgvaracct.getText().toString() + "'" + ","
                            + "pl_out_ratevar = " + "'" + tboutratevaracct.getText().toString() + "'"
                            + " where pl_line = " + "'" + tbkey.getText().toString() + "'"
                            + ";");
                    m = new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.updateRecordSuccess};
                    initvars(null);
                } 
         
            } catch (SQLException s) {
                MainFrame.bslog(s);
                m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName())};  
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
            m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1020, Thread.currentThread().getStackTrace()[1].getMethodName())};
        }
     
     return m;
     }
     
    public String[] deleteRecord(String[] x) {
     String[] m = new String[2];
        boolean proceed = bsmf.MainFrame.warn("Are you sure?");
        if (proceed) {
        try {

           Connection con = DriverManager.getConnection(url + db, user, pass);
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                   int i = st.executeUpdate("delete from pl_mstr where pl_line = " + "'" + tbkey.getText() + "'" +  ";");
                    if (i > 0) {
                    m = new String[] {BlueSeerUtils.SuccessBit, BlueSeerUtils.deleteRecordSuccess};
                    initvars(null);
                    }
                } catch (SQLException s) {
                 MainFrame.bslog(s); 
                m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName())};  
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
            m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1020, Thread.currentThread().getStackTrace()[1].getMethodName())};
        }
        } else {
           m = new String[] {BlueSeerUtils.ErrorBit, BlueSeerUtils.deleteRecordCanceled}; 
        }
     return m;
     }
      
    public String[] getRecord(String[] x) {
       String[] m = new String[2];
       
        try {

            Connection con = DriverManager.getConnection(url + db, user, pass);
            Statement st = con.createStatement();
            ResultSet res = null;
            try {
                
                int i = 0;
                res = st.executeQuery("SELECT * FROM  pl_mstr where pl_line = " + "'" + x[0] + "'" + ";");
                    while (res.next()) {
                        i++;
                         tbkey.setText(res.getString("pl_line"));
                         tbdesc.setText(res.getString("pl_desc"));
                         tbinvacct.setText(res.getString("pl_inventory"));
                         tbinvdescrepancyacct.setText(res.getString("pl_inv_discr"));
                         tbscrapacct.setText(res.getString("pl_scrap"));
                         tbwipacct.setText(res.getString("pl_wip"));
                         tbwipvaracct.setText(res.getString("pl_wip_var"));
                         tbinvchangeacct.setText(res.getString("pl_inv_change"));
                         tbsalesacct.setText(res.getString("pl_sales"));
                         tbsalesdiscacct.setText(res.getString("pl_sales_disc"));
                         tbcogsmtlacct.setText(res.getString("pl_cogs_mtl"));
                         tbcogslbracct.setText(res.getString("pl_cogs_lbr"));
                         tbcogsbdnacct.setText(res.getString("pl_cogs_bdn"));
                         tbcogsovhacct.setText(res.getString("pl_cogs_ovh"));
                         tbcogsoutacct.setText(res.getString("pl_cogs_out"));
                         tbpurchacct.setText(res.getString("pl_purchases"));
                         tbporcptacct.setText(res.getString("pl_po_rcpt"));
                         tbpoovhacct.setText(res.getString("pl_po_ovh"));
                         tbpopricevaracct.setText(res.getString("pl_po_pricevar"));
                         tbapusageacct.setText(res.getString("pl_ap_usage"));
                         tbapratevaracct.setText(res.getString("pl_ap_ratevar"));
                         tbjobstockacct.setText(res.getString("pl_job_stock"));
                         tbmtlusagevaracct.setText(res.getString("pl_mtl_usagevar"));
                         tbmtlratevaracct.setText(res.getString("pl_mtl_ratevar"));
                         tbmixedvaracct.setText(res.getString("pl_mix_var"));
                         tbcopacct.setText(res.getString("pl_cop"));
                         tboutusgvaracct.setText(res.getString("pl_out_usagevar"));
                         tboutratevaracct.setText(res.getString("pl_out_ratevar"));
                    }
               
                // set Action if Record found (i > 0)
                m = setAction(i);
                
            } catch (SQLException s) {
                MainFrame.bslog(s);
                m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName())};  
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
            m = new String[]{BlueSeerUtils.ErrorBit, getMessageTag(1020, Thread.currentThread().getStackTrace()[1].getMethodName())};  
        }
      return m;
    }
    
    public void lookUpFrame() {
        
        luinput.removeActionListener(lual);
        lual = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
        if (lurb1.isSelected()) {  
         luModel = DTData.getProdCodeBrowseUtil(luinput.getText(),0, "pl_line");
        } else {
         luModel = DTData.getProdCodeBrowseUtil(luinput.getText(),0, "pl_desc");   
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
                initvars(new String[]{target.getValueAt(row,1).toString(), target.getValueAt(row,2).toString()});
                }
            }
        };
        luTable.addMouseListener(luml);
      
        callDialog(getClassLabelTag("lblid", this.getClass().getSimpleName()), getClassLabelTag("lbldesc", this.getClass().getSimpleName())); 
         
        
        
    }

    // custom funcs
    
    
    
   
    /**
     * This method is called from within the bsmf.MainFrame.constructor to initialize the form.
     * WARNING: Do NOT modify this code. The bsmf.MainFrame.content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tbwipacct = new javax.swing.JTextField();
        tbsalesacct = new javax.swing.JTextField();
        jLabel75 = new javax.swing.JLabel();
        tbinvacct = new javax.swing.JTextField();
        jLabel71 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        btupdate = new javax.swing.JButton();
        btadd = new javax.swing.JButton();
        tbdesc = new javax.swing.JTextField();
        jLabel69 = new javax.swing.JLabel();
        jLabel73 = new javax.swing.JLabel();
        tbwipvaracct = new javax.swing.JTextField();
        jLabel72 = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        tbscrapacct = new javax.swing.JTextField();
        tbkey = new javax.swing.JTextField();
        jLabel74 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        tbinvchangeacct = new javax.swing.JTextField();
        tbsalesdiscacct = new javax.swing.JTextField();
        tbinvdescrepancyacct = new javax.swing.JTextField();
        tbcogsmtlacct = new javax.swing.JTextField();
        jLabel76 = new javax.swing.JLabel();
        tbcogslbracct = new javax.swing.JTextField();
        tbcogsbdnacct = new javax.swing.JTextField();
        tbcogsovhacct = new javax.swing.JTextField();
        tbcogsoutacct = new javax.swing.JTextField();
        tbpurchacct = new javax.swing.JTextField();
        tbporcptacct = new javax.swing.JTextField();
        tbpoovhacct = new javax.swing.JTextField();
        jLabel77 = new javax.swing.JLabel();
        jLabel78 = new javax.swing.JLabel();
        jLabel79 = new javax.swing.JLabel();
        jLabel80 = new javax.swing.JLabel();
        jLabel81 = new javax.swing.JLabel();
        jLabel82 = new javax.swing.JLabel();
        jLabel83 = new javax.swing.JLabel();
        tbpopricevaracct = new javax.swing.JTextField();
        jLabel84 = new javax.swing.JLabel();
        jLabel85 = new javax.swing.JLabel();
        jLabel86 = new javax.swing.JLabel();
        tboutratevaracct = new javax.swing.JTextField();
        jLabel87 = new javax.swing.JLabel();
        jLabel88 = new javax.swing.JLabel();
        tbtbd = new javax.swing.JTextField();
        jLabel89 = new javax.swing.JLabel();
        jLabel90 = new javax.swing.JLabel();
        jLabel91 = new javax.swing.JLabel();
        jLabel92 = new javax.swing.JLabel();
        tbapusageacct = new javax.swing.JTextField();
        tbcopacct = new javax.swing.JTextField();
        tbmixedvaracct = new javax.swing.JTextField();
        tboutusgvaracct = new javax.swing.JTextField();
        tbapratevaracct = new javax.swing.JTextField();
        jLabel93 = new javax.swing.JLabel();
        tbmtlratevaracct = new javax.swing.JTextField();
        tbmtlusagevaracct = new javax.swing.JTextField();
        tbjobstockacct = new javax.swing.JTextField();
        jLabel94 = new javax.swing.JLabel();
        btnew = new javax.swing.JButton();
        btdelete = new javax.swing.JButton();
        btclear = new javax.swing.JButton();
        btlookup = new javax.swing.JButton();

        setBackground(new java.awt.Color(0, 102, 204));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Product Code Maintenance"));
        jPanel1.setName("panelmain"); // NOI18N

        jLabel75.setText("Inv Descrepancy");
        jLabel75.setName("lblinvdiscrepancy"); // NOI18N

        jLabel71.setText("Wip Variance Acct");
        jLabel71.setName("lblwipvar"); // NOI18N

        jLabel66.setText("ProdCode");
        jLabel66.setName("lblid"); // NOI18N

        btupdate.setText("Update");
        btupdate.setName("btupdate"); // NOI18N
        btupdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btupdateActionPerformed(evt);
            }
        });

        btadd.setText("Add");
        btadd.setName("btadd"); // NOI18N
        btadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btaddActionPerformed(evt);
            }
        });

        jLabel69.setText("Scrap Acct");
        jLabel69.setName("lblscrap"); // NOI18N

        jLabel73.setText("Sales Acct");
        jLabel73.setName("lblsales"); // NOI18N

        jLabel72.setText("Inventory Change Acct");
        jLabel72.setName("lblinvchange"); // NOI18N

        jLabel70.setText("Wip Acct");
        jLabel70.setName("lblwip"); // NOI18N

        tbkey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbkeyActionPerformed(evt);
            }
        });

        jLabel74.setText("Sales Discount Acct");
        jLabel74.setName("lblsalesdisc"); // NOI18N

        jLabel67.setText("Description");
        jLabel67.setName("lbldesc"); // NOI18N

        jLabel68.setText("Inventory Acct");
        jLabel68.setName("lblinv"); // NOI18N

        jLabel76.setText("COGS Mtl Acct");
        jLabel76.setName("lblcogsmtl"); // NOI18N

        jLabel77.setText("COGS Lbr Acct");
        jLabel77.setName("lblcogslbr"); // NOI18N

        jLabel78.setText("COGS Bdn Acct");
        jLabel78.setName("lblcogsbdn"); // NOI18N

        jLabel79.setText("COGS Ovh Acct");
        jLabel79.setName("lblcogsovh"); // NOI18N

        jLabel80.setText("COGS Out Acct");
        jLabel80.setName("lblcogsout"); // NOI18N

        jLabel81.setText("Purchases Acct");
        jLabel81.setName("lblpurchases"); // NOI18N

        jLabel82.setText("PO Receipt Acct");
        jLabel82.setName("lblporeceipt"); // NOI18N

        jLabel83.setText("PO Ovh Acct");
        jLabel83.setName("lblpoovh"); // NOI18N

        jLabel84.setText("PO Pricevar Acct");
        jLabel84.setName("lblpopricevar"); // NOI18N

        jLabel85.setText("Matl Usage Var Acct");
        jLabel85.setName("lblmtlusagevar"); // NOI18N

        jLabel86.setText("Matl Rate Var Acct");
        jLabel86.setName("lblmtlratevar"); // NOI18N

        jLabel87.setText("AP Rate Var Acct");
        jLabel87.setName("lblapratevar"); // NOI18N

        jLabel88.setText("TBD");
        jLabel88.setName("lbltbd"); // NOI18N

        jLabel89.setText("Outside Rate Var Acct");
        jLabel89.setName("lbloutratevar"); // NOI18N

        jLabel90.setText("Outside Usage Var Acct");
        jLabel90.setName("lbloutusagevar"); // NOI18N

        jLabel91.setText("Cost Of Prod Acct");
        jLabel91.setName("lblcostofprod"); // NOI18N

        jLabel92.setText("Mixed Var Acct");
        jLabel92.setName("lblmixedvar"); // NOI18N

        jLabel93.setText("AP Usage Acct");
        jLabel93.setName("lblapusage"); // NOI18N

        jLabel94.setText("Job Stock Acct");
        jLabel94.setName("lbljobstock"); // NOI18N

        btnew.setText("New");
        btnew.setName("btnew"); // NOI18N
        btnew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnewActionPerformed(evt);
            }
        });

        btdelete.setText("Delete");
        btdelete.setName("btdelete"); // NOI18N
        btdelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btdeleteActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel70)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbwipacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel69)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbscrapacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel68)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbinvacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel71)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbwipvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel72)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbinvchangeacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel73)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbsalesacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel74)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbsalesdiscacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel75)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbinvdescrepancyacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel66)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel94)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tbjobstockacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btdelete)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btadd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btupdate))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel76, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel77, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel78, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel79, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel80, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel81, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel82, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel83, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel84, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(btlookup, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnew)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btclear)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tbcogslbracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbcogsmtlacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbcogsbdnacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbcogsovhacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbcogsoutacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbpurchacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbporcptacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbpoovhacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbpopricevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel93, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel87, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel85, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel86, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel92, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel91, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel90, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel89, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel88, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tbapratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbapusageacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbmtlusagevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbmtlratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbmixedvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbcopacct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tboutusgvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tboutratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tbtbd, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(29, 29, 29)
                                .addComponent(jLabel67)
                                .addGap(9, 9, 9)
                                .addComponent(tbdesc, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbdesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel67)
                        .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel66)
                        .addComponent(btnew)
                        .addComponent(btclear))
                    .addComponent(btlookup))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbinvacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel68)
                            .addComponent(tbcogsmtlacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel76))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbinvdescrepancyacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel75)
                            .addComponent(tbcogslbracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel77))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbscrapacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel69)
                            .addComponent(tbcogsbdnacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel78))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbwipacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel70)
                            .addComponent(tbcogsovhacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel79))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbwipvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel71)
                            .addComponent(tbcogsoutacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel80))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbinvchangeacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel72)
                            .addComponent(tbpurchacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel81))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbsalesacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel73)
                            .addComponent(tbporcptacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel82))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbsalesdiscacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel74)
                            .addComponent(tbpoovhacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel83))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbpopricevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel84)
                            .addComponent(tbjobstockacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel94)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbapusageacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel93))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbapratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel87))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbmtlusagevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel85))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbmtlratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel86))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbmixedvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel92))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbcopacct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel91))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tboutusgvaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel90))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tboutratevaracct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel89))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbtbd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel88))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btadd)
                    .addComponent(btupdate)
                    .addComponent(btdelete))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void btaddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btaddActionPerformed
        if (! validateInput("addRecord")) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask("add", new String[]{tbkey.getText()});
    }//GEN-LAST:event_btaddActionPerformed

    private void btupdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btupdateActionPerformed
          if (! validateInput("updateRecord")) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask("update", new String[]{tbkey.getText()});
    }//GEN-LAST:event_btupdateActionPerformed

    private void btnewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnewActionPerformed
        newAction("");
    }//GEN-LAST:event_btnewActionPerformed

    private void btdeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btdeleteActionPerformed
        if (! validateInput("deleteRecord")) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask("delete", new String[]{tbkey.getText()});   
    }//GEN-LAST:event_btdeleteActionPerformed

    private void tbkeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbkeyActionPerformed
       executeTask("get", new String[]{tbkey.getText()});
    }//GEN-LAST:event_tbkeyActionPerformed

    private void btclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btclearActionPerformed
        BlueSeerUtils.messagereset();
        initvars(null);
    }//GEN-LAST:event_btclearActionPerformed

    private void btlookupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btlookupActionPerformed
        lookUpFrame();
    }//GEN-LAST:event_btlookupActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btadd;
    private javax.swing.JButton btclear;
    private javax.swing.JButton btdelete;
    private javax.swing.JButton btlookup;
    private javax.swing.JButton btnew;
    private javax.swing.JButton btupdate;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel69;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JLabel jLabel85;
    private javax.swing.JLabel jLabel86;
    private javax.swing.JLabel jLabel87;
    private javax.swing.JLabel jLabel88;
    private javax.swing.JLabel jLabel89;
    private javax.swing.JLabel jLabel90;
    private javax.swing.JLabel jLabel91;
    private javax.swing.JLabel jLabel92;
    private javax.swing.JLabel jLabel93;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField tbapratevaracct;
    private javax.swing.JTextField tbapusageacct;
    private javax.swing.JTextField tbcogsbdnacct;
    private javax.swing.JTextField tbcogslbracct;
    private javax.swing.JTextField tbcogsmtlacct;
    private javax.swing.JTextField tbcogsoutacct;
    private javax.swing.JTextField tbcogsovhacct;
    private javax.swing.JTextField tbcopacct;
    private javax.swing.JTextField tbdesc;
    private javax.swing.JTextField tbinvacct;
    private javax.swing.JTextField tbinvchangeacct;
    private javax.swing.JTextField tbinvdescrepancyacct;
    private javax.swing.JTextField tbjobstockacct;
    private javax.swing.JTextField tbkey;
    private javax.swing.JTextField tbmixedvaracct;
    private javax.swing.JTextField tbmtlratevaracct;
    private javax.swing.JTextField tbmtlusagevaracct;
    private javax.swing.JTextField tboutratevaracct;
    private javax.swing.JTextField tboutusgvaracct;
    private javax.swing.JTextField tbpoovhacct;
    private javax.swing.JTextField tbpopricevaracct;
    private javax.swing.JTextField tbporcptacct;
    private javax.swing.JTextField tbpurchacct;
    private javax.swing.JTextField tbsalesacct;
    private javax.swing.JTextField tbsalesdiscacct;
    private javax.swing.JTextField tbscrapacct;
    private javax.swing.JTextField tbtbd;
    private javax.swing.JTextField tbwipacct;
    private javax.swing.JTextField tbwipvaracct;
    // End of variables declaration//GEN-END:variables
}
