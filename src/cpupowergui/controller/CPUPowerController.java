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
            if( ex.getMessage().contains("No such file or directory") || ex.getMessage().contains("but can be installed") ) {
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
            String command = "pkexec cpupower frequency-set --governor " + governorString;
            
//            // apply fixed configuration
//            if( cpuPowerJFrame.getjCheckBox1().isSelected() ) { 
//                command += "& sed -i 's/^GOVERNOR=.*/GOVERNOR=\"" + governorString + "\"/' /etc/init.d/cpufrequtils";
//                //command += "| grep -v  '#' /etc/init.d/cpufrequtils  | awk -F \"=\" '/GOVERNOR=/ {print $2;}'";
//            }
            
            Runtime rt = Runtime.getRuntime();            
            //Process pr = rt.exec("pkexec cpupower frequency-set --governor " + governorString); 
            Process pr = rt.exec(new String[] { "bash", "-c", command});
            
            String output;
            final ProcessResultReader stderr = new ProcessResultReader(pr.getErrorStream(), "STDERR");
            stderr.run();            
            output =  stderr.toString();  
            System.out.println(output);
            
            
            final ProcessResultReader stdout = new ProcessResultReader(pr.getErrorStream(), "STDOUT");
            stdout.run();            
            output =  stdout.toString();  
            System.out.println(output);            
        } catch (IOException ex) {
            if( ex.getMessage().contains("No such file or directory") || ex.getMessage().contains("but can be installed") ) {
                JOptionPane.showMessageDialog(cpuPowerJFrame, "Programa 'cpupower' não encontrado, se essa distribuição do Linux for baseada"
                        + "\nno Ubuntu, tente instalar executando o comando: 'sudo apt install linux-tools-common' e"
                        + "\n'sudo apt-get install -y linux-tools-$(uname -r)'",
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }            
            Logger.getLogger(CPUPowerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        governor = getCurrentGovernor();
        cpuPowerJFrame.setStatus( governor.name() ); 
        
        // apply fixed configuration
        if( cpuPowerJFrame.getjCheckBox1().isSelected() ) {                        
            try {                        
                Runtime rt = Runtime.getRuntime();
                String command = "pkexec sed -i 's/^GOVERNOR=.*/GOVERNOR=\"" + governorString + "\"/' /etc/init.d/cpufrequtils";
                Process pr = rt.exec(new String[] { "bash", "-c", command});
                
//                command = "pkexec grep -v  '#' /etc/init.d/cpufrequtils | awk -F \"=\" '/GOVERNOR=/ {print $2;}'";                
//                pr = rt.exec(new String[] { "bash", "-c", command});
                
                String output;  
                
                final ProcessResultReader stderr = new ProcessResultReader(pr.getErrorStream(), "STDERR");
                stderr.run();            
                output =  stderr.toString();  
                System.out.println(output);                
                
                final ProcessResultReader stdout = new ProcessResultReader(pr.getErrorStream(), "STDOUT");
                stdout.run();            
                output =  stdout.toString();  
                System.out.println(output);                 
                JOptionPane.showMessageDialog(cpuPowerJFrame, "Governor permanently altered","Message",JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(CPUPowerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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

    public void showAbout() {
        String text;
        
        text = "Desenvolvido por Robson Santana\n" +
                "email: robson.poli@gmail.com\n" +
                "versão: 1.1, 2019\n" +
                "apoio: Prefeitura Municipal de Flores-PE (2017-2020)";
        
        JOptionPane.showMessageDialog(null, text, "Sobre", JOptionPane.INFORMATION_MESSAGE);
    }

    private enum Governor { PERFORMANCE, ONDEMAND, CONSERVATIVE, POWERSAVE, USERSPACE, UNKNOWN };
    private Governor governor;
    
}
