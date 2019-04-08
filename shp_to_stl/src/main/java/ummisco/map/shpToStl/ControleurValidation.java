package ummisco.map.shpToStl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class ControleurValidation implements ActionListener{
	
	private ArrayList<File> liste_shapefile = new ArrayList<File>();
	private JFrame fenetre;
	private JFrame fenetredebut;
	private JFormattedTextField decoupe;
	private JFormattedTextField taille;
	private JTextField hauteur;

	public ControleurValidation(JFrame fenetredebut,JFrame fenetre,JFormattedTextField decoupe,JFormattedTextField taille,JTextField hauteur,ArrayList<File> liste_shapefile ){
		this.fenetre=fenetre;
		this.fenetredebut=fenetredebut;
		this.liste_shapefile=liste_shapefile;
		this.decoupe=decoupe;
		this.taille=taille;
		this.hauteur=hauteur;
	}

	
	//Verifit le bouton clique
	@Override
	public void actionPerformed(ActionEvent e) {
		String text = e.getActionCommand();
		if(text.equals("Retour")){
			fenetredebut.setVisible(true);
			fenetre.setVisible(false);
		}
		if(text.equals("OK")){
			int taillle;
			int coupe;
			String haut;
			String coupetxt = decoupe.getText().toString().replaceAll(" ", "");
			String coupetxt2 = taille.getText().toString().replaceAll(" ", "");
			if(taille.getText().equals("") || taille.getText().equals("0"))
				taillle=1;
			else
				taillle=Integer.parseInt(coupetxt2);
			if(decoupe.getText().equals("") || decoupe.getText().equals("0"))
				coupe=1;
			else
				coupe = Integer.parseInt(coupetxt);
			if(hauteur.getText().equals(""))
				haut="Error";
			else
				haut = hauteur.getText();
			System.out.println("coupe "+ coupe+" taille"+ taillle+" haut "+haut);
			Conversion conv = new Conversion(coupe,taillle,haut);	
			try {
				conv.parcoursFichier(liste_shapefile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			JOptionPane.showMessageDialog(fenetre,"Termine !","", JOptionPane.INFORMATION_MESSAGE);
			fenetre.dispose();
			fenetredebut.dispose();
			Interface fenetre = new Interface();
			fenetre.fenetreApp();
		}
	}
}
