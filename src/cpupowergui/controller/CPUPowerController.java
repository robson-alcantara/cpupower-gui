/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cpupowergui.controller;

import cpupowergui.view.CPUPowerJFrame;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;

/**
 *
 * @author secretaria
 */
public class CPUPowerController {
    
    private CPUPowerJFrame cpuPowerJFrame;
    
    public void run() {
    
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CPUPowerController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CPUPowerController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CPUPowerController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CPUPowerController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        cpuPowerJFrame = new CPUPowerJFrame();
        cpuPowerJFrame.setController( this );
        governor = getCurrentGovernor();
        cpuPowerJFrame.setStatus( governor.name() );

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                cpuPowerJFrame.setVisible(true);
            }
        });    
    }
    
    private Governor getCurrentGovernor() {     
        Governor governor = Governor.UNKNOWN;
        try {
            boolean knownLanguage = true;
            Locale locale = Locale.getDefault();
            String headString = "";
            
            if(locale.getDisplayLanguage().compareTo("português")==0) {
                headString = "O regulador";
            }
            
            else if(locale.getDisplayLanguage().compareTo("english")==0) {
                headString = "The governor";
            }
            
            else {
                knownLanguage = false;
            }            
            
            if( knownLanguage ) {
                String output;            
                Runtime rt = Runtime.getRuntime();
                Process pr = rt.exec("cpufreq-info"); 
                final ProcessResultReader stdout = new ProcessResultReader(pr.getInputStream(), "STDOUT");
                stdout.run();            
                output =  stdout.toString();
                output = output.substring(output.indexOf(headString) + 13);
                output = output.substring(0,output.indexOf(" ") - 1 );
                output = output.toUpperCase();
                governor = Governor.valueOf(output);
            }

        } catch (IOException ex) {            
            if( ex.getMessage().contains("No such file or directory") ) {
                JOptionPane.showMessageDialog(cpuPowerJFrame, "Programa 'cpufreq-info' não encontrado, se essa distribuição do Linux for baseada"
                        + "\nno Ubuntu, tente instalar executando o comando: 'sudo apt install cpufrequtils'",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
            
            Logger.getLogger(CPUPowerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return governor;
    }   
    
    public void setGovernor() {
        String governorString = getSelectedButtonText( cpuPowerJFrame.getButtonGroup1() );              
        
        try {         
            Runtime rt = Runtime.getRuntime();
            String[] command = {"gksudo","cpupower frequency-set --governor " + governorString};
            Process pr = rt.exec(command);
            
            String output;
            final ProcessResultReader stdout = new ProcessResultReader(pr.getErrorStream(), "STDERR");
            stdout.run();            
            output =  stdout.toString();  
            System.out.println(output);
        } catch (IOException ex) {
            if( ex.getMessage().contains("No such file or directory") ) {
                JOptionPane.showMessageDialog(cpuPowerJFrame, "Programa 'cpupower' não encontrado, se essa distribuição do Linux for baseada"
                        + "\nno Ubuntu, tente instalar executando o comando: 'sudo apt install linux-tools-common' e"
                        + "\n'sudo apt-get install -y linux-tools-$(uname -r)'",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }            
            Logger.getLogger(CPUPowerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        governor = getCurrentGovernor();
        cpuPowerJFrame.setStatus( governor.name() );        
    }        

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }

        return null;
    }    

    private enum Governor { PERFORMANCE, ONDEMAND, CONSERVATIVE, POWERSAVE, USERSPACE, UNKNOWN };
    private Governor governor;
    
}
