package ummisco.map.shpToStl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class InterfaceValidation {

	private JFrame fenetre;
	private JPanel panel;
	private JPanel panel2;
	private JLabel label;
	private JLabel label2;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	private JFormattedTextField decoupe;
	private JFormattedTextField taille;
	private JTextField hauteur;
	private JButton retour;
	private JButton ok;
	private Color couleur;
	private NumberFormat contrainte;
	private ControleurValidation controleur;
	
	public InterfaceValidation( ArrayList<File> liste_shapefile, JFrame fenetre2){
		this.fenetre = new JFrame("ShapeSTL");
		this.retour = new JButton("Retour");
		this.ok = new JButton("OK");
		this.panel = new JPanel();
		this.couleur = new Color(250,250,250);
		this.label = new JLabel("ShapeFile to STL");
		this.label2 = new JLabel("DECOUPAGE");
		this.label4 = new JLabel("Taille des morceaux (cm) :");
		this.label3 = new JLabel("Nom de la variable pour l'altitude : ");
		this.label5 = new JLabel("Taille de la maquette (cm) : ");
		this.panel2 = new JPanel(new GridLayout(0,2));
		this.contrainte = NumberFormat.getIntegerInstance();
		this.contrainte.setMaximumFractionDigits(0);
		this.decoupe = new JFormattedTextField(contrainte);
		this.taille = new JFormattedTextField(contrainte);
		this.hauteur = new JTextField();
		this.controleur = new ControleurValidation(fenetre2,fenetre,decoupe,taille,hauteur,liste_shapefile);
	}


	//Affiche la fenetre de l'application
	public void fenetreApp(){
		fenetre.setSize(500,500);
		fenetre.setLocation(150,60);
		fenetre.setResizable(false);
		fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		label.setFont(new Font("Arial",Font.BOLD,50));
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setForeground(new Color(Integer.parseInt("#302CB8".replaceFirst("#",""),16)));
		label.setBackground(couleur);
		label.setOpaque(true);	
		label2.setHorizontalAlignment(JLabel.CENTER);
		label3.setHorizontalAlignment(JLabel.CENTER);
		label4.setHorizontalAlignment(JLabel.CENTER);
		label5.setHorizontalAlignment(JLabel.CENTER);
		panel2.setBackground(couleur);
		panel2.add(label2);
		panel2.add(new JLabel());
		panel2.add(label5);
		panel2.add(taille);
		panel2.add(label4);
		panel2.add(decoupe);
		panel2.add(label3);
		panel2.add(hauteur);
		retour.addActionListener(controleur);
		ok.addActionListener(controleur);
		panel.add(retour);
		panel.add(ok);
		panel.setBackground(couleur);
		fenetre.add(label,BorderLayout.NORTH);
		fenetre.add(panel2,BorderLayout.CENTER);
		fenetre.add(panel,BorderLayout.SOUTH);
		fenetre.setVisible(true);
	}
}
