package com.linxonline.malleteditor.system ;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.media.opengl.awt.GLCanvas ;

public class MainPanel extends JPanel
{
	private JMenuBar menubar = new JMenuBar() ;
	private JLabel infoLabel = new JLabel( "Info: " );
	private JList entityList ;
	
	public MainPanel( final GLCanvas _canvas )
	{
		final DefaultListModel defaultList = new DefaultListModel() ;
		defaultList.addElement( "TEST TEST TEST" ) ;

		entityList = new JList( defaultList ) ;
		
		final JMenu file = menubar.add( new JMenu( "File" ) ) ;
		file.add( new JMenuItem( "Open" ) ) ;
		file.add( new JMenuItem( "Save" ) ) ;

		setLayout( new BorderLayout( 1, 0 ) ) ;
		add( menubar,    BorderLayout.NORTH ) ;
		add( entityList, BorderLayout.WEST  ) ;
		add( infoLabel,  BorderLayout.SOUTH ) ;
		add( _canvas, BorderLayout.CENTER ) ;
	}
}
