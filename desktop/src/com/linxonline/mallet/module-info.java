module MalletEngine
{
	requires joal ;
	requires jogl.all.noawt ;
	requires gluegen.rt ;
	requires org.json ;
	requires org.mozilla.rhino ;

	exports com.linxonline.mallet.animation ;
	exports com.linxonline.mallet.audio ;
	exports com.linxonline.mallet.audio.desktop.alsa ;
	exports com.linxonline.mallet.core ;
	exports com.linxonline.mallet.core.desktop ;
	exports com.linxonline.mallet.core.desktop.gl ;
	exports com.linxonline.mallet.core.test ;
	exports com.linxonline.mallet.ecs ;
	exports com.linxonline.mallet.entity ;
	exports com.linxonline.mallet.entity.components ;
	exports com.linxonline.mallet.event ;
	exports com.linxonline.mallet.input ;
	exports com.linxonline.mallet.input.desktop ;
	exports com.linxonline.mallet.io ;
	exports com.linxonline.mallet.io.filesystem ;
	exports com.linxonline.mallet.io.filesystem.desktop ;
	exports com.linxonline.mallet.io.formats.gltf ;
	exports com.linxonline.mallet.io.formats.json ;
	exports com.linxonline.mallet.io.formats.ogg ;
	exports com.linxonline.mallet.io.formats.sgeom ;
	exports com.linxonline.mallet.io.formats.wav ;
	exports com.linxonline.mallet.io.language ;
	exports com.linxonline.mallet.io.net ;
	exports com.linxonline.mallet.io.reader ;
	exports com.linxonline.mallet.io.reader.config ;
	exports com.linxonline.mallet.io.serialisation ;
	exports com.linxonline.mallet.io.writer ;
	exports com.linxonline.mallet.io.writer.config ;
	exports com.linxonline.mallet.maths ;
	exports com.linxonline.mallet.physics ;
	exports com.linxonline.mallet.renderer ;
	exports com.linxonline.mallet.renderer.opengl ;
	exports com.linxonline.mallet.renderer.desktop.opengl ;
	exports com.linxonline.mallet.script ;
	exports com.linxonline.mallet.script.javascript ;
	exports com.linxonline.mallet.ui ;
	exports com.linxonline.mallet.ui.gui ;
	exports com.linxonline.mallet.util ;
	exports com.linxonline.mallet.util.buffers ;
	exports com.linxonline.mallet.util.caches ;
	exports com.linxonline.mallet.util.inspect ;
	exports com.linxonline.mallet.util.notification ;
	exports com.linxonline.mallet.util.settings ;
	exports com.linxonline.mallet.util.time ;
	exports com.linxonline.mallet.util.tools ;
}
