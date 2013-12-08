package com.linxonline.malleteditor.system ;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.io.File ;

import javax.media.opengl.awt.GLCanvas ;

import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.malleteditor.system.EditorState ;

public class MainPanel extends JPanel
{
	private final static String[] EVENT_TYPES = { "NONE" } ; 
	private EventController eventController ;
	private final JMenuBar menubar = new JMenuBar() ;
	private final JLabel infoLabel = new JLabel( "Info: " );
	private JList entityList ;

	public MainPanel( final GLCanvas _canvas )
	{
		initSideList() ;
		initMenuBar() ;
		initEventController() ;

		final JPanel entityListPanel = new JPanel() ;
		entityListPanel.setLayout( new BorderLayout( 1, 0 ) ) ;
		entityListPanel.add( entityList, BorderLayout.CENTER ) ;

		setLayout( new BorderLayout( 1, 0 ) ) ;
		add( menubar,    BorderLayout.NORTH ) ;
		add( entityListPanel, BorderLayout.WEST  ) ;
		add( infoLabel,  BorderLayout.SOUTH ) ;
		add( _canvas, BorderLayout.CENTER ) ;
	}

	protected void initSideList()
	{
		final DefaultListModel<EntityCell> defaultList = new DefaultListModel<EntityCell>() ;
		defaultList.addElement( new EntityCell() ) ;

		entityList = new JList( defaultList ) ;
		entityList.setFixedCellWidth( 150 ) ;

		final JPopupMenu entityMenu = new JPopupMenu() ;
		final JMenuItem addEntity = entityMenu.add( new JMenuItem( "Create Entity" ) ) ;
		addEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				System.out.println( "Create Entity" ) ;
			}
		} ) ;

		final JMenuItem removeEntity = entityMenu.add( new JMenuItem( "Remove Entity" ) ) ;
		removeEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				System.out.println( "Remove Entity" ) ;
			}
		} ) ;

		final JMenuItem copyEntity = entityMenu.add( new JMenuItem( "Copy Entity" ) ) ;
		copyEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				System.out.println( "Copy Entity" ) ;
			}
		} ) ;

		final JMenuItem pasteEntity = entityMenu.add( new JMenuItem( "Paste Entity" ) ) ;
		pasteEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				System.out.println( "Paste Entity" ) ;
			}
		} ) ;

		entityList.setComponentPopupMenu( entityMenu ) ;
	}

	protected void initMenuBar()
	{
		final JMenu fileMenu = menubar.add( new JMenu( "File" ) ) ;
		final JMenuItem open = fileMenu.add( new JMenuItem( "Open" ) ) ;
		open.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				final JFileChooser chooser = new JFileChooser() ;
				chooser.setCurrentDirectory( new File( "." ) ) ;
				int option = chooser.showOpenDialog( MainPanel.this ) ;

				switch( option )
				{
					case JFileChooser.APPROVE_OPTION :
					{
						final File file = chooser.getSelectedFile() ;
						eventController.passEvent( new Event( EditorState.EVENT_TYPES[0], file.getAbsolutePath() ) ) ;
					}
				}
			}
		} ) ;

		final JMenuItem importFile = fileMenu.add( new JMenuItem( "Import" ) ) ;
		importFile.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				eventController.passEvent( new Event( EditorState.EVENT_TYPES[1], null ) ) ;
			}
		} ) ;
		
		final JMenuItem save = fileMenu.add( new JMenuItem( "Save" ) ) ;
		save.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				eventController.passEvent( new Event( EditorState.EVENT_TYPES[2], null ) ) ;
			}
		} ) ;

		final JMenuItem exit = fileMenu.add( new JMenuItem( "Exit" ) ) ;
		exit.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				System.out.println( "Exit..." ) ;
				System.exit( 0 ) ;
			}
		} ) ;
	}

	protected void initEventController()
	{
		eventController = new EventController()
		{
			//	Override processEvent, as storing Events via the 
			//	EventControllers messenger wont get updated.
			@Override
			public void processEvent( final Event _event )
			{
				System.out.println( "Recieved Event..." ) ;
			}
		} ;
		eventController.setWantedEventTypes( EVENT_TYPES ) ;
	}

	public EventController getEventController()
	{
		return eventController ;
	}
}
