/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author jaam
 */
public class Metodos {
    public String[] AbrirArchivo(String Texto,String Formato){
     JFileChooser dialogo=new JFileChooser();
     dialogo.setMultiSelectionEnabled(true);
     dialogo.setFileFilter(new FileNameExtensionFilter(Texto,Formato));
        int opcion=dialogo.showOpenDialog(null); 
        if(opcion==JFileChooser.APPROVE_OPTION){
            File Archivos[]=dialogo.getSelectedFiles();
            String Ubicaciones[]=new String[Archivos.length];
            int i=0;
            for(File Aux:Archivos){
                Ubicaciones[i]=Aux.getPath();
                i++;
            }
            return Ubicaciones;
        }   
            return null;
    }
    
    public String[] AbrirCarpeta(){        
      JFileChooser dialogo=new JFileChooser();
      dialogo.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int opcion=dialogo.showOpenDialog(null); 
        if(opcion==JFileChooser.APPROVE_OPTION){
            String Ruta=dialogo.getSelectedFile().getPath();
            File directorio = new File(dialogo.getSelectedFile().getPath());
            String listaDirectorio[] = directorio.list();
            int tamano=listaDirectorio.length;
            for(int i=0;i<tamano;i++){
                listaDirectorio[i]=Ruta+"\\"+listaDirectorio[i];
            }
            return listaDirectorio;
        }else{
            return null;
        }    
    }
    
    public boolean EsMp3(String ruta){ String ext=""; boolean enc=false;
        int tam=ruta.length();
        for(int i=0;i<tam;i++){
            if(ruta.charAt(i)=='.'){
                enc=true; ext="";
            }else if(enc){
                ext+=ruta.charAt(i);
            }
        }
        return "mp3".equalsIgnoreCase(ext);
    }
    
    public String FormatoReloj(long T_segundo){
        long Segundo=T_segundo%60;
        long Minuto=T_segundo/60;
        String TSeg = ""+Segundo;
        String TMin = ""+Minuto;
        if (Segundo<10){
            TSeg = "0"+Segundo;
        }
        if (Minuto<10){
            TMin = "0"+Minuto;
        }
        return TMin+":"+TSeg;
    }
    
    public static void CrearCarpeta(){
        File Folder=new File(Ruta());
        if(!Folder.exists()){
            Folder.mkdir();
        }   
    }
    
    public static String Ruta(){
        return System.getProperty("user.home")+"/StyleOfMusik";
    }
}
