/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cpupowergui;

import cpupowergui.controller.CPUPowerController;

/**
 *
 * @author secretaria
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        CPUPowerController cpuPowerController = new CPUPowerController();
        cpuPowerController.run();        
    }
    
}
