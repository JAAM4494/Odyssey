/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import odyssey.ui.LoginFrame;
import odyssey.ui.OdysseyFrame;

/**
 *
 * @author jaam
 */
public class Odyssey {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Metal".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        //LoginFrame ventanaLogin = new LoginFrame();
        //ventanaLogin.setVisible(true);
        OdysseyFrame ventanaOdyssey = new OdysseyFrame();
        ventanaOdyssey.run();
    }
}
