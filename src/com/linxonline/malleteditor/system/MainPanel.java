package com.linxonline.malleteditor.system ;

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import javax.swing.event.* ;
import java.io.File ;

import javax.media.opengl.awt.GLCanvas ;
import javax.media.opengl.awt.GLJPanel ;

import com.linxonline.mallet.entity.Entity ;
import com.linxonline.mallet.event.EventController ;
import com.linxonline.mallet.event.Event ;

import com.linxonline.malleteditor.system.EditorState ;

public class MainPanel extends JPanel
{
	public final static String[] EVENT_TYPES = { "ADD_ENTITY_TO_LIST", "REMOVE_ENTITY_TO_LIST" } ; 

	private EventController eventController ;
	private final JMenuBar menubar = new JMenuBar() ;
	private final JLabel infoLabel = new JLabel( "Info: " );

	private JList entityList = null ;
	private final DefaultListModel<EntityCell> defaultList = new DefaultListModel<EntityCell>() ;

	public MainPanel( final GLJPanel _canvas )
	{
		initSideList() ;
		initMenuBar() ;
		initEventController() ;

		final JPanel entityListPanel = new JPanel() ;
		entityListPanel.setLayout( new BorderLayout( 1, 0 ) ) ;
		entityListPanel.add( entityList, BorderLayout.CENTER ) ;

		setLayout( new BorderLayout( 1, 0 ) ) ;
		add( _canvas, BorderLayout.CENTER ) ;
		add( menubar,    BorderLayout.NORTH ) ;
		add( entityListPanel, BorderLayout.WEST  ) ;
		add( infoLabel,  BorderLayout.SOUTH ) ;
	}

	protected void initSideList()
	{
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

		final JMenuItem editEntity = entityMenu.add( new JMenuItem( "Edit Entity" ) ) ;
		editEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				final EntityCell cell = defaultList.get( entityList.getSelectedIndex() ) ;
				if( cell != null )
				{
					final Entity entity = cell.getEntity() ;
					System.out.println( "Edit Entity: " + entity.id.name ) ;
				}
			}
		} ) ;
		
		final JMenuItem removeEntity = entityMenu.add( new JMenuItem( "Remove Entity" ) ) ;
		removeEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				final int index = entityList.getSelectedIndex() ;
				final EntityCell cell = defaultList.get( index ) ;
				if( cell != null )
				{
					final Entity entity = cell.getEntity() ;
					eventController.passEvent( new Event( "REMOVE_ENTITY", entity ) ) ;
					defaultList.remove( index ) ;
				}
			}
		} ) ;

		final JMenuItem copyEntity = entityMenu.add( new JMenuItem( "Copy Entity" ) ) ;
		copyEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				final EntityCell cell = defaultList.get( entityList.getSelectedIndex() ) ;
				if( cell != null )
				{
					final Entity entity = cell.getEntity() ;
					System.out.println( "Copy Entity: " + entity.id.name ) ;
				}
			}
		} ) ;

		final JMenuItem pasteEntity = entityMenu.add( new JMenuItem( "Paste Entity" ) ) ;
		pasteEntity.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				final EntityCell cell = defaultList.get( entityList.getSelectedIndex() ) ;
				if( cell != null )
				{
					final Entity entity = cell.getEntity() ;
					System.out.println( "Paste Entity: " + entity.id.name ) ;
				}
			}
		} ) ;

		// Open Popup Menu on the closest cell in the list
		entityList.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( MouseEvent _event )
			{
				if( SwingUtilities.isRightMouseButton( _event ) == true )
				{
					int index = entityList.locationToIndex( _event.getPoint() ) ;
					entityList.setSelectedIndex( index ) ;
					if( entityList.isSelectionEmpty() == false )
					{
						entityMenu.show( entityList, _event.getX(), _event.getY() ) ;
					}
				}
			}
		} ) ;
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
						eventController.passEvent( new Event( "OPEN_FILE", file.getAbsolutePath() ) ) ;
					}
				}
			}
		} ) ;

		final JMenuItem importFile = fileMenu.add( new JMenuItem( "Import" ) ) ;
		importFile.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				eventController.passEvent( new Event( "IMPORT_FILE", null ) ) ;
			}
		} ) ;
		
		final JMenuItem save = fileMenu.add( new JMenuItem( "Save" ) ) ;
		save.addActionListener( new ActionListener()
		{
			public void actionPerformed( ActionEvent _event )
			{
				eventController.passEvent( new Event( "SAVE_FILE", null ) ) ;
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
				if( _event.isEventByString( EVENT_TYPES[0] ) == true )
				{
					// Add Entity to List
					final Entity entity = ( Entity )_event.getVariable() ;
					defaultList.addElement( new EntityCell( entity ) ) ;
				}
				else if( _event.isEventByString( EVENT_TYPES[1] ) == true )
				{
					// Remove Entity from List
					final Entity entity = ( Entity )_event.getVariable() ;
					final int size = defaultList.size() ;
					for( int i = 0; i < size; ++i )
					{
						final EntityCell cell = defaultList.getElementAt( i ) ;
						if( entity == cell.getEntity() )
						{
							defaultList.remove( i ) ;
							return ;
						}
					}
				}
			}
		} ;
		eventController.setWantedEventTypes( EVENT_TYPES ) ;
	}

	public EventController getEventController()
	{
		return eventController ;
	}
}
