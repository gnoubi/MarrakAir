package ummisco.map.shpToStl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Controleur implements ActionListener{

	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private ArrayList<JButton> liste_bouton = new ArrayList<JButton>();
	private ArrayList<JLabel> liste_nomfichier = new ArrayList<JLabel>();
	private ArrayList<String> liste_cpt = new ArrayList<String>();
	private JFrame fenetre;
	private FileFilter shp;
	private JPanel panel;
	private int cpt;

	public Controleur(JFrame fenetre, JPanel panel){
		this.fenetre=fenetre;
		this.panel=panel;
		this.shp = new FileNameExtensionFilter("ShapeFile","shp");
		this.cpt=0;
	}


	//Verifit le bouton clique
	@Override
	public void actionPerformed(ActionEvent e) {
		String text = e.getActionCommand();
		if(text.equals("Nouveau ShapeFile")){
			choixFichier();
		}
		else if(text.equals("Suivant")){
			if(liste_shapefile.size()!=0){
				fenetre.setVisible(false);
				InterfaceValidation interval = new InterfaceValidation(liste_shapefile,fenetre);
				interval.fenetreApp();
			}
		}
		else{
			supprimeShapeFile(e);
		}	
		fenetre.revalidate();
		fenetre.repaint();
	}


	//Ajoute un ShapeFile
	public void choixFichier(){
		JFileChooser exploreur = new JFileChooser(".");
		exploreur.setFileFilter(shp);
		int res = exploreur.showOpenDialog(fenetre);
		if(res==JFileChooser.APPROVE_OPTION){
			File nomfichier = exploreur.getSelectedFile();
			JLabel fichier = new JLabel(nomfichier.getName());
			fichier.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(fichier);
			JButton bouton = new JButton("Supprimer");
			bouton.setActionCommand(Integer.toString(cpt));
			bouton.addActionListener(this);
			panel.add(bouton);
			liste_cpt.add(Integer.toString(cpt));
			cpt++;
			liste_shapefile.add(nomfichier);
			liste_nomfichier.add(fichier);
			liste_bouton.add(bouton);
		}
	}


	//Supprime ShapeFile
	public void supprimeShapeFile(ActionEvent e){
		if(liste_shapefile.size()!=0){
			int index=0;
			while(!liste_cpt.get(index).equals(e.getActionCommand())){
				index++;
			}
			liste_shapefile.remove(index);
			panel.remove(liste_bouton.get(index));
			panel.remove(liste_nomfichier.get(index));
			liste_nomfichier.remove(index);
			liste_bouton.remove(index);
			liste_cpt.remove(index);
		}
	}
}
