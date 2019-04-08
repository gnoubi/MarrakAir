package ummisco.map.shpToStl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Interface{

	private JFrame fenetre;
	private JPanel panel;
	private JPanel panel2;
	private JLabel label;
	private JButton choix;
	private JButton valide;
	private Color couleur;
	private JScrollPane scroll;
	private Controleur controleur;
	
	public Interface(){
		this.fenetre = new JFrame("ShapeSTL");
		this.choix = new JButton("Nouveau ShapeFile");
		this.valide = new JButton("Suivant");
		this.panel = new JPanel();
		this.couleur = new Color(250,250,250);
		this.label = new JLabel("ShapeFile to STL");
		this.panel2 = new JPanel(new GridLayout(0,2));
		this.scroll = new JScrollPane(panel2);
		this.controleur = new Controleur(fenetre,panel2);
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
		panel2.setBackground(couleur);
		choix.addActionListener(controleur);
		valide.addActionListener(controleur);
		panel.add(choix);
		panel.add(valide);
		panel.setBackground(couleur);
		fenetre.add(label,BorderLayout.NORTH);
		fenetre.add(scroll,BorderLayout.CENTER);
		fenetre.add(panel,BorderLayout.SOUTH);
		fenetre.setVisible(true);
	}
}


