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
package com.blueseer.far;

import bsmf.MainFrame;
import static bsmf.MainFrame.db;
import static bsmf.MainFrame.defaultDecimalSeparator;
import static bsmf.MainFrame.ds;
import static bsmf.MainFrame.pass;
import static bsmf.MainFrame.tags; 
import static bsmf.MainFrame.url;
import static bsmf.MainFrame.user;
import com.blueseer.ctr.cusData;
import static com.blueseer.ctr.cusData.getCustInfo;
import static com.blueseer.far.farData.addArTransaction;
import com.blueseer.far.farData.ar_mstr;
import com.blueseer.far.farData.ard_mstr;
import com.blueseer.fgl.fglData;
import com.blueseer.utl.BlueSeerUtils;
import static com.blueseer.utl.BlueSeerUtils.bsParseDouble;
import static com.blueseer.utl.BlueSeerUtils.callDialog;
import static com.blueseer.utl.BlueSeerUtils.currformatDouble;
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
import static com.blueseer.utl.BlueSeerUtils.parseDate;
import com.blueseer.utl.DTData;
import com.blueseer.utl.IBlueSeer;
import com.blueseer.utl.OVData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
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
import javax.swing.event.TableModelEvent;


/**
 *
 * @author vaughnte
 */
public class ARPaymentMaint extends javax.swing.JPanel implements IBlueSeer {

    // global variable declarations
                boolean isLoad = false;
                String terms = "";
                String aracct = "";
                String arcc = "";
                String arbank = "";
                double actamt = 0.00;
                double control = 0.00;
                double baseamt = 0.00;
                double rcvamt = 0.00;
                String curr = "";
                String basecurr = "";
                
    
    // global datatablemodel declarations 
    javax.swing.table.DefaultTableModel referencemodel = new javax.swing.table.DefaultTableModel(new Object[][]{},
            new String[]{
                getGlobalColumnTag("reference"), 
                getGlobalColumnTag("type"), 
                getGlobalColumnTag("duedate"), 
                getGlobalColumnTag("amount"), 
                getGlobalColumnTag("applied"), 
                getGlobalColumnTag("open"), 
                getGlobalColumnTag("tax"), 
                getGlobalColumnTag("currency")});
    javax.swing.table.DefaultTableModel armodel = new javax.swing.table.DefaultTableModel(new Object[][]{},
            new String[]{
                getGlobalColumnTag("reference"), 
                getGlobalColumnTag("amount"), 
                getGlobalColumnTag("tax"), 
                getGlobalColumnTag("currency")
            });
                
    javax.swing.event.TableModelListener ml = new javax.swing.event.TableModelListener() {
                    @Override
                    public void tableChanged(TableModelEvent tme) {
                        if (tme.getType() == TableModelEvent.UPDATE && (tme.getColumn() == 1 )) {
                            sumdollars();
                        }
                        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };            
  
    public ARPaymentMaint() {
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
         terms = "";
         aracct = "";
         arcc = "";
         arbank = "";
         actamt = 0.00;
         control = 0.00;
         rcvamt = 0.00;
        
         basecurr = OVData.getDefaultCurrency();
         
        lbcust.setText("");
        lbmessage.setText("");
        lbmessage.setForeground(Color.blue);
                
        tbrmks.setText("");
        tbcontrolamt.setText("0");
        tbcontrolamt.setBackground(Color.white);
        tbcheck.setText("");
        tbactualamt.setText("0");
        tbactualamt.setBackground(Color.white);
        tbactualamt.setEditable(false);
        tbrefamt.setText("0");
        referencemodel.setRowCount(0);
        armodel.setRowCount(0);
        armodel.addTableModelListener(ml);
        referencedet.setModel(referencemodel);
        ardet.setModel(armodel);
        referencedet.getTableHeader().setReorderingAllowed(false);
        ardet.getTableHeader().setReorderingAllowed(false);
       
        
        java.util.Date now = new java.util.Date();
        dcdate.setDate(now);
              
        ddcust.removeAllItems();
        ArrayList mycust = cusData.getcustmstrlist();
        for (int i = 0; i < mycust.size(); i++) {
            ddcust.addItem(mycust.get(i));
        }
        ddcust.insertItemAt("", 0);
        ddcust.setSelectedIndex(0);
          
        ddsite.removeAllItems();
        ArrayList mylist = OVData.getSiteList();
        for (int i = 0; i < mylist.size(); i++) {
            ddsite.addItem(mylist.get(i));
        }
        ddsite.setSelectedItem(OVData.getDefaultSite());
        
        ddcurr.removeAllItems();
        ArrayList<String> curr = fglData.getCurrlist();
        for (int i = 0; i < curr.size(); i++) {
            ddcurr.addItem(curr.get(i));
        }
        ddcurr.setSelectedItem(OVData.getDefaultCurrency());
        
        
       isLoad = false;
    }
    
    public void newAction(String x) {
       setPanelComponentState(this, true);
        setComponentDefaultValues();
        BlueSeerUtils.message(new String[]{"0",BlueSeerUtils.addRecordInit});
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
                   
                   tbactualamt.setText(currformatDouble(actamt));
                   tbcontrolamt.setText(currformatDouble(actamt));
        } else {
           m = new String[]{BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordError};  
                   tbkey.setForeground(Color.red); 
        }
        return m;
    }
    
    public boolean validateInput(String x) {
        boolean b = true;
                if (ddsite.getSelectedItem() == null || ddsite.getSelectedItem().toString().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1026));
                    ddsite.requestFocus();
                    return b;
                }
               
                if (ddcurr.getSelectedItem() == null || ddcurr.getSelectedItem().toString().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1026));
                    ddcurr.requestFocus();
                    return b;
                }
                
                if (tbkey.getText().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1024));
                    tbkey.requestFocus();
                    return b;
                }
                
                   
                 if (tbcheck.getText().isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1127));
                    tbcheck.requestFocus();
                    return b;
                }
                if (arbank.isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1128));
                    return b;
                }
                if (arcc.isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1129));
                    return b;
                }
                if (aracct.isEmpty()) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1130));
                    return b;
                }
                 if ( control != actamt || control == 0.00 || actamt == 0.00 ) {
                    b = false;
                    bsmf.MainFrame.show(getMessageTag(1039,String.valueOf(control)));
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
        String[] m = addArTransaction(createDetRecord(), createRecord());
        boolean error = OVData.ARUpdate(tbkey.getText());
        if (! error)
        error = fglData.glEntryFromARPayment(tbkey.getText(), dcdate.getDate());
        return m;
    }
     
    public String[] updateRecord(String[] x) {
     String[] m = new String[]{BlueSeerUtils.ErrorBit, "This update functionality is not implemented at this time"};
     return m;
     }
     
    public String[] deleteRecord(String[] x) {
     String[] m = new String[]{BlueSeerUtils.ErrorBit, "This delete functionality is not implemented at this time"};
     return m;
     }
      
    public String[] getRecord(String[] x) {
       String[] m = new String[2];
        
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
                
                int i = 0;
                int d = 0;
                res = st.executeQuery("select * from ar_mstr where ar_nbr = " + "'" + x[0] + "'" + ";");
                while (res.next()) {
                  // "Reference", "AmountToApply", "TaxAmount", "Curr"
                  i++;
                     tbkey.setText(res.getString("ar_nbr"));
                     dcdate.setDate(parseDate(res.getString("ar_effdate")));
                     tbcheck.setText(res.getString("ar_ref"));
                     tbrmks.setText(res.getString("ar_rmks"));
                     ddcust.setSelectedItem(res.getString("ar_cust"));
                     ddsite.setSelectedItem(res.getString("ar_site"));
                     ddcurr.setSelectedItem(res.getString("ar_curr"));
                }
                
                res = st.executeQuery("select * from ard_mstr where ard_id = " + "'" + x[0] + "'" + ";");
                while (res.next()) {
                  // "Reference", "AmountToApply", "TaxAmount", "Curr"
                     armodel.addRow(new Object[] { res.getString("ard_ref"),
                                              res.getString("ard_amt"),
                                              res.getString("ard_amt_tax"),
                                              res.getString("ard_curr")
                                              });
                 
                  
                  actamt += res.getDouble("ard_amt");
                d++;
                }
               
                // set Action if Record found (i > 0)
                m = setAction(i);
                
            } catch (SQLException s) {
                MainFrame.bslog(s);
                m = new String[]{BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordSQLError};  
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
            m = new String[]{BlueSeerUtils.ErrorBit, BlueSeerUtils.getRecordConnError};  
        }
      return m;
    }
    
    public ar_mstr createRecord() { 
        java.util.Date now = new java.util.Date(); 
        
                if (basecurr.toUpperCase().equals(ddcurr.getSelectedItem().toString().toUpperCase())) {
                  baseamt = actamt;  
                } else {
                  baseamt = OVData.getExchangeBaseValue(basecurr, ddcurr.getSelectedItem().toString(), actamt);
                } 
        ar_mstr x = new ar_mstr(null, 
                null, // ar_id auto-generated
                tbkey.getText(),
                ddcust.getSelectedItem().toString(),
                currformatDouble(actamt).replace(defaultDecimalSeparator, '.'),
                currformatDouble(baseamt).replace(defaultDecimalSeparator, '.'),
                "P",
                ddcurr.getSelectedItem().toString(),
                basecurr,
                tbcheck.getText(),
                tbrmks.getText(),
                BlueSeerUtils.setDateFormatNull(now),
                BlueSeerUtils.setDateFormatNull(dcdate.getDate()),
                BlueSeerUtils.setDateFormatNull(now),
                aracct,
                arcc,
                "c",
                arbank,
                ddsite.getSelectedItem().toString(),
                "0", //ar_amt_tax
                "0", //ar_base_amt_tax
                "0", //ar_amt_disc
                "0", //ar_base_amt_disc
                "0", //ar_open_amt
                "0", //ar_applied
                "", //ar_terms
                "", //ar_tax_code
                BlueSeerUtils.setDateFormatNull(null), //ar_invdate
                BlueSeerUtils.setDateFormatNull(null), //ar_duedate
                BlueSeerUtils.setDateFormatNull(null), //ar_discdate
                "0" //ar_reverse
                );
        return x;
    }
   
    public ArrayList<ard_mstr> createDetRecord() {
        ArrayList<ard_mstr> list = new ArrayList<ard_mstr>();
        
            
            double amt_d = 0;
            double taxamt_d = 0;
            double baseamt_d = 0;
            double basetaxamt_d = 0;
            boolean completeAllocation = true;
            for (int j = 0; j < ardet.getRowCount(); j++) {
                        amt_d = bsParseDouble(ardet.getValueAt(j, 1).toString());
                        taxamt_d = bsParseDouble(ardet.getValueAt(j, 2).toString());
                         if (basecurr.toUpperCase().equals(ddcurr.getSelectedItem().toString().toUpperCase())) {
                         baseamt_d = amt_d;
                         basetaxamt_d = taxamt_d;
                         } else {
                         baseamt_d = OVData.getExchangeBaseValue(basecurr, ddcurr.getSelectedItem().toString(), amt_d);
                         basetaxamt_d = OVData.getExchangeBaseValue(basecurr, ddcurr.getSelectedItem().toString(), taxamt_d);
                         }
                        ard_mstr x = new ard_mstr(null,  
                            tbkey.getText(), 
                            String.valueOf(j + 1),    
                            ddcust.getSelectedItem().toString(),
                            ardet.getValueAt(j, 0).toString(),
                            BlueSeerUtils.setDateFormatNull(dcdate.getDate()),
                            currformatDouble(amt_d).replace(defaultDecimalSeparator, '.'),
                            currformatDouble(taxamt_d).replace(defaultDecimalSeparator, '.'),
                            currformatDouble(baseamt_d).replace(defaultDecimalSeparator, '.'),     
                            currformatDouble(basetaxamt_d).replace(defaultDecimalSeparator, '.'),
                            ddcurr.getSelectedItem().toString(),
                            basecurr,
                            aracct,
                            arcc
                            ); 
                    list.add(x);
                    }
        return list;
    }
    
    public void lookUpFrame() {
        
        luinput.removeActionListener(lual);
        lual = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
        if (lurb1.isSelected()) {  
         luModel = DTData.getARPaymentBrowseUtil(luinput.getText(),0, "ar_nbr");
        } else {
         luModel = DTData.getARPaymentBrowseUtil(luinput.getText(),0, "ar_cust");   
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
      
        callDialog(getClassLabelTag("lblbatch", this.getClass().getSimpleName()), 
                getClassLabelTag("lblbillto", this.getClass().getSimpleName()));  
        
        
    }

    
    // custom funcs      
    public void setcustvariables(String cust) {
       
        // aracct, arcc, currency, bank, terms, carrier, onhold, site
            String[] custinfo = getCustInfo(cust);
            aracct = custinfo[0];
            arcc = custinfo[1];
            terms = custinfo[4];
            arbank = custinfo[3];
       
    }
      
    public void getreferences(String cust) {
        referencemodel.setRowCount(0);
        try {
            Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
            int i = 0;
            int d = 0;
            String uniqpo = null;
            try {
                rcvamt = 0.00;
                res = st.executeQuery("select * from ar_mstr where ar_cust = " + "'" + cust + "'" +
                        " AND ar_curr = " + "'" + curr + "'" + 
                        " AND ar_status = 'o' " + ";");
                while (res.next()) {
                  referencemodel.addRow(new Object[]{res.getString("ar_nbr"), 
                      res.getString("ar_type"), 
                      res.getString("ar_duedate"), 
                      currformatDouble(res.getDouble("ar_amt")), 
                      res.getDouble("ar_applied"), 
                      currformatDouble(res.getDouble("ar_open_amt")),
                      currformatDouble(res.getDouble("ar_amt_tax")),
                      res.getString("ar_curr")});
                  
                  rcvamt += res.getDouble("ar_open_amt");
                d++;
                }
                tbrefamt.setText(currformatDouble(rcvamt));
                referencedet.setModel(referencemodel);

            } catch (SQLException s) {
                MainFrame.bslog(s);
                bsmf.MainFrame.show(getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName()));
            } finally {
               if (res != null) res.close();
               if (st != null) st.close();
               if (con != null) con.close();
            }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
    }
           
    public void setstatus(javax.swing.JTable mytable) {
            try {
            Connection con = null;
            if (ds != null) {
              con = ds.getConnection();
            } else {
              con = DriverManager.getConnection(url + db, user, pass);  
            }
            Statement st = con.createStatement();
            ResultSet res = null;
             
             String sonbr = null;
             int i = 0;
             int qty = 0;
             boolean iscomplete = true;
             String sodstatus = "";
             
        // find the sod record for each line / So pair
        for (int j = 0; j < mytable.getRowCount(); j++) {
               //    mytable.getModel().getValueAt(j, 0);  
             i = 0;
             String thispart = mytable.getModel().getValueAt(j, 0).toString();
             String thisorder = mytable.getModel().getValueAt(j, 1).toString();
             String thispo = mytable.getModel().getValueAt(j, 2).toString();
             double thisrecvqty = Double.valueOf(mytable.getModel().getValueAt(j, 3).toString());
             String thislinestatus = "";
             double thisrecvpedtotal = 0;
            
             try {
            
                 /* ok....let's get the current state of this line item on the sales order */
                 res = st.executeQuery("select * from sod_det where sod_nbr = " + "'" + thisorder + "'" + 
                                     " AND sod_item = " + "'" + thispart + "'" + ";");
               while (res.next()) {     
                 i++;
                   if (Double.valueOf(res.getString("sod_recvped_qty") + thisrecvqty) < Double.valueOf(res.getString("sod_ord_qty")) ) {
                   thislinestatus = "Partial"; 
                   }
                   if (Double.valueOf(res.getString("sod_recvped_qty") + thisrecvqty) >= Double.valueOf(res.getString("sod_ord_qty")) ) {
                   thislinestatus = "Shipped"; 
                   }
                   thisrecvpedtotal = thisrecvqty + Double.valueOf(res.getString("sod_recvped_qty"));
                   
                }
                 
                 /* ok...now lets update the status of this sod_det line item */
                 st.executeUpdate("update sod_det set sod_recvped_qty = " + "'" + thisrecvpedtotal + "'" + 
                                  "," + " sod_status = " + "'" + thislinestatus + "'" +
                                  " where sod_nbr = " + "'" + thisorder + "'" + 
                                  " and sod_item = " + "'" + thispart + "'" + 
                                  " and sod_po = " + "'" + thispo + "'" +
                     ";");
                 
             } catch (SQLException s) {
                 MainFrame.bslog(s);
                 bsmf.MainFrame.show(getMessageTag(1016, Thread.currentThread().getStackTrace()[1].getMethodName()));
             } finally {
                if (res != null) {
                    res.close();
                }
                if (st != null) {
                    st.close();
                }
                con.close();
             }
         }
        } catch (Exception e) {
            MainFrame.bslog(e);
        }
    }
    
    public void sumdollars() {
        double dol = 0;
        double summaryTaxPercent = 0;
        double headertax = 0;
        double matltax = 0;
        double totaltax = 0;
        
        actamt = 0;
         for (int j = 0; j < ardet.getRowCount(); j++) {
             actamt += bsParseDouble(ardet.getModel().getValueAt(j,1).toString());
         }
         
          if (ardet.getRowCount() >= 1) {
             ddcurr.setEnabled(false);
           }
         if (control == actamt && control != 0.00 ) {
             tbcontrolamt.setBackground(Color.green);
             tbactualamt.setBackground(Color.green);
         } else {
            tbcontrolamt.setBackground(Color.white); 
            tbactualamt.setBackground(Color.white);
         }
        tbactualamt.setText(currformatDouble(actamt));
        
    }
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        tbkey = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        btnew = new javax.swing.JButton();
        tbcontrolamt = new javax.swing.JTextField();
        jLabel36 = new javax.swing.JLabel();
        btadditem = new javax.swing.JButton();
        btadd = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        ardet = new javax.swing.JTable();
        ddcust = new javax.swing.JComboBox();
        btdeleteitem = new javax.swing.JButton();
        dcdate = new com.toedter.calendar.JDateChooser();
        jLabel27 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        referencedet = new javax.swing.JTable();
        tbcheck = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        tbactualamt = new javax.swing.JTextField();
        btaddall = new javax.swing.JButton();
        tbrefamt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        tbrmks = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        ddsite = new javax.swing.JComboBox();
        jLabel37 = new javax.swing.JLabel();
        ddcurr = new javax.swing.JComboBox<>();
        jLabel38 = new javax.swing.JLabel();
        lbmessage = new javax.swing.JLabel();
        lbcust = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btclear = new javax.swing.JButton();
        btlookup = new javax.swing.JButton();

        jLabel1.setText("jLabel1");

        setBackground(new java.awt.Color(0, 102, 204));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("AR Payment Maintenance"));
        jPanel1.setName("panelmain"); // NOI18N

        tbkey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbkeyActionPerformed(evt);
            }
        });

        jLabel24.setText("Batch Nbr");
        jLabel24.setName("lblbatch"); // NOI18N

        btnew.setText("New");
        btnew.setName("btnew"); // NOI18N
        btnew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnewActionPerformed(evt);
            }
        });

        tbcontrolamt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tbcontrolamtFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tbcontrolamtFocusLost(evt);
            }
        });

        jLabel36.setText("Billto");
        jLabel36.setName("lblbillto"); // NOI18N

        btadditem.setText("Add Item");
        btadditem.setName("btadditem"); // NOI18N
        btadditem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btadditemActionPerformed(evt);
            }
        });

        btadd.setText("Add");
        btadd.setName("btadd"); // NOI18N
        btadd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btaddActionPerformed(evt);
            }
        });

        ardet.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane7.setViewportView(ardet);

        ddcust.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddcustActionPerformed(evt);
            }
        });

        btdeleteitem.setText("Del Item");
        btdeleteitem.setName("btdeleteitem"); // NOI18N
        btdeleteitem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btdeleteitemActionPerformed(evt);
            }
        });

        dcdate.setDateFormatString("yyyy-MM-dd");

        jLabel27.setText("Control Amt");
        jLabel27.setName("lblcontrol"); // NOI18N

        jLabel35.setText("EffDate");
        jLabel35.setName("lbleffdate"); // NOI18N

        referencedet.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane8.setViewportView(referencedet);

        jLabel2.setText("CheckNbr");
        jLabel2.setName("lblchecknbr"); // NOI18N

        btaddall.setText("Add All");
        btaddall.setName("btaddall"); // NOI18N
        btaddall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btaddallActionPerformed(evt);
            }
        });

        jLabel3.setText("Ref Total");
        jLabel3.setName("lblreftotal"); // NOI18N

        jLabel4.setText("Rmks");
        jLabel4.setName("lblremarks"); // NOI18N

        ddsite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddsiteActionPerformed(evt);
            }
        });

        jLabel37.setText("Site");
        jLabel37.setName("lblsite"); // NOI18N

        ddcurr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddcurrActionPerformed(evt);
            }
        });

        jLabel38.setText("Currency");
        jLabel38.setName("lblcurrency"); // NOI18N

        jLabel5.setText("ActualAmt");
        jLabel5.setName("lblactual"); // NOI18N

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
                        .addComponent(btadditem)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btdeleteitem))
                    .addComponent(btadd)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(9, 9, 9)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel24)
                                .addComponent(jLabel36)
                                .addComponent(jLabel4)
                                .addComponent(jLabel37)
                                .addComponent(jLabel38))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(tbrmks)
                                    .addGap(215, 215, 215))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btlookup, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(33, 33, 33)
                                            .addComponent(btnew)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btclear))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(ddcurr, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(ddcust, javax.swing.GroupLayout.Alignment.LEADING, 0, 119, Short.MAX_VALUE)
                                                .addComponent(ddsite, javax.swing.GroupLayout.Alignment.LEADING, 0, 119, Short.MAX_VALUE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(lbcust, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(tbrefamt, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btaddall)
                                                .addGap(14, 14, 14))
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel27, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addComponent(jLabel35, javax.swing.GroupLayout.Alignment.TRAILING))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(dcdate, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                            .addComponent(tbactualamt)
                                                            .addComponent(tbcontrolamt, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                                                        .addGap(10, 10, 10)
                                                        .addComponent(jLabel2)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(tbcheck, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGap(22, 22, 22)))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(lbmessage, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(0, 105, Short.MAX_VALUE))))))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnew)
                                .addComponent(tbkey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel24)
                                .addComponent(btclear))
                            .addComponent(btlookup))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(ddcust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel36)
                            .addComponent(lbcust, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ddsite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel37))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ddcurr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel38)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(lbmessage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tbcontrolamt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel27)
                            .addComponent(tbcheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(tbactualamt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dcdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel35))
                        .addGap(9, 9, 9)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(tbrmks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbrefamt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(btaddall))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btdeleteitem)
                    .addComponent(btadditem))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(btadd)
                .addGap(35, 35, 35))
        );

        add(jPanel1);
    }// </editor-fold>//GEN-END:initComponents

    private void btnewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnewActionPerformed
        newAction("ar");
    }//GEN-LAST:event_btnewActionPerformed

    private void btadditemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btadditemActionPerformed
        boolean canproceed = true;
              
       // Pattern p = Pattern.compile("\\d\\.\\d\\d");
      //  Matcher m = p.matcher(tbprice.getText());
       // "Reference", "Type", "DueDate", "Amount", "AmtApplied", "AmtOpen"
       // "Reference" "Amount"
         int[] rows = referencedet.getSelectedRows();
        for (int i : rows) {
           armodel.addRow(new Object[] { referencedet.getModel().getValueAt(i, 0),
                                              referencedet.getModel().getValueAt(i, 5),
                                              referencedet.getModel().getValueAt(i, 6),
                                              referencedet.getModel().getValueAt(i, 7)
                                              });
        }
        
        sumdollars();
        
    }//GEN-LAST:event_btadditemActionPerformed

    private void btaddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btaddActionPerformed
       if (! validateInput("addRecord")) {
           return;
       }
        setPanelComponentState(this, false);
        executeTask("add", new String[]{tbkey.getText()});
    }//GEN-LAST:event_btaddActionPerformed

    private void ddcustActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddcustActionPerformed
        // clean slate
        referencemodel.setRowCount(0);
        armodel.setRowCount(0);
        lbcust.setText("");
        if ( ddcust.getSelectedItem() != null && ! ddcust.getSelectedItem().toString().isEmpty()  && ! isLoad) {
        ddcurr.setSelectedItem(cusData.getCustCurrency(ddcust.getSelectedItem().toString()));
        lbcust.setText(cusData.getCustName(ddcust.getSelectedItem().toString()));
        getreferences(ddcust.getSelectedItem().toString());
        setcustvariables(ddcust.getSelectedItem().toString());
        }
    }//GEN-LAST:event_ddcustActionPerformed

    private void btdeleteitemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btdeleteitemActionPerformed
        int[] rows = ardet.getSelectedRows();
        for (int i : rows) {
            bsmf.MainFrame.show(getMessageTag(1031,String.valueOf(i)));
             actamt -= bsParseDouble(ardet.getModel().getValueAt(i,1).toString());
            ((javax.swing.table.DefaultTableModel) ardet.getModel()).removeRow(i);
        }
        tbactualamt.setText(String.valueOf(actamt));
    }//GEN-LAST:event_btdeleteitemActionPerformed

    private void btaddallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btaddallActionPerformed
          for (int i = 0; i < referencedet.getRowCount(); i++) {
            actamt += bsParseDouble(referencedet.getModel().getValueAt(i,5).toString());
            
           armodel.addRow(new Object[] { referencedet.getModel().getValueAt(i, 0),
                                              referencedet.getModel().getValueAt(i, 5),
                                              referencedet.getModel().getValueAt(i, 6),
                                              referencedet.getModel().getValueAt(i, 7)
                                              });
        }
        
       if (control == actamt && control != 0.00 ) {
             tbcontrolamt.setBackground(Color.green);
             tbactualamt.setBackground(Color.green);
         } else {
            tbcontrolamt.setBackground(Color.white); 
            tbactualamt.setBackground(Color.white);
         }
        tbactualamt.setText(currformatDouble(actamt));
    }//GEN-LAST:event_btaddallActionPerformed

    private void ddsiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddsiteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ddsiteActionPerformed

    private void ddcurrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddcurrActionPerformed
        if (ddcust.getSelectedItem() != null &&  ddcurr.getSelectedItem() != null && ! isLoad ) {
        curr = ddcurr.getSelectedItem().toString();
        getreferences(ddcust.getSelectedItem().toString());
        }
    }//GEN-LAST:event_ddcurrActionPerformed

    private void tbcontrolamtFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tbcontrolamtFocusGained
       if (tbcontrolamt.getText().equals("0")) {
            tbcontrolamt.setText("");
        }
    }//GEN-LAST:event_tbcontrolamtFocusGained

    private void tbcontrolamtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tbcontrolamtFocusLost
           String x = BlueSeerUtils.bsformat("", tbcontrolamt.getText(), "2");
        if (x.equals("error")) {
            tbcontrolamt.setText("");
            tbcontrolamt.setBackground(Color.yellow);
            bsmf.MainFrame.show(getMessageTag(1000));
            tbcontrolamt.requestFocus();
        } else {
            tbcontrolamt.setText(x);
            tbcontrolamt.setBackground(Color.white);
        }
        
        if (! tbcontrolamt.getText().isEmpty()) {
            control = bsParseDouble(tbcontrolamt.getText());
        } else {
            tbcontrolamt.setText("0.00");
            control = 0.00;
        }
        
       if (control == actamt && control != 0.00 ) {
             tbcontrolamt.setBackground(Color.green);
             tbactualamt.setBackground(Color.green);
         } else {
            tbcontrolamt.setBackground(Color.white); 
            tbactualamt.setBackground(Color.white);
         }
       
    }//GEN-LAST:event_tbcontrolamtFocusLost

    private void btclearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btclearActionPerformed
        BlueSeerUtils.messagereset();
        initvars(null);
    }//GEN-LAST:event_btclearActionPerformed

    private void tbkeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbkeyActionPerformed
        executeTask("get", new String[]{tbkey.getText()});
    }//GEN-LAST:event_tbkeyActionPerformed

    private void btlookupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btlookupActionPerformed
        lookUpFrame();
    }//GEN-LAST:event_btlookupActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable ardet;
    private javax.swing.JButton btadd;
    private javax.swing.JButton btaddall;
    private javax.swing.JButton btadditem;
    private javax.swing.JButton btclear;
    private javax.swing.JButton btdeleteitem;
    private javax.swing.JButton btlookup;
    private javax.swing.JButton btnew;
    private com.toedter.calendar.JDateChooser dcdate;
    private javax.swing.JComboBox<String> ddcurr;
    private javax.swing.JComboBox ddcust;
    private javax.swing.JComboBox ddsite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JLabel lbcust;
    private javax.swing.JLabel lbmessage;
    private javax.swing.JTable referencedet;
    private javax.swing.JTextField tbactualamt;
    private javax.swing.JTextField tbcheck;
    private javax.swing.JTextField tbcontrolamt;
    private javax.swing.JTextField tbkey;
    private javax.swing.JTextField tbrefamt;
    private javax.swing.JTextField tbrmks;
    // End of variables declaration//GEN-END:variables
}
