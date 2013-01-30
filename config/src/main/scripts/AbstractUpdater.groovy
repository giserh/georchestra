/**
 * The 'path' parameter [Optional] a path to add to end of both to and from.
 * The 'fromProject' parameter [Optional] if non-null it is the directory name of one of the root projects
 * The 'from' parameter [Optional] is a string and is relative to the root of the config project directory
 * The 'to' parameter is also a string but is relative to the directory that contains generated files.  These files override the
 * files in the normal configuration
 * The update method is called with a closure that will update the properties read from the 'from' file and writes the updated properties
 * to the 'to' file.  
 * 
 * Note: if the 'from' parameter is not defined then the properties will be empty when passed to the update method.
 * Note: if the 'from' parameter is defined then the file must exist or an error will be thrown
 * Note: if any of the parameters have a / it will be replaced with the platform specific path separator
 */
abstract class AbstractUpdater {
  String path
	String from, to
	String fromProject
	
	protected def getParams() {
	  return Parameters.get
  }
	
	protected File getFromFile() {
	  if(from == null) {
	    return null;
	  }
	  def fromFile;

	  if(fromProject != null) {
	    fromFile = new File(params.basedirFile.parent+("/$fromProject/$from").replace('/', File.separator))
	  } else {
	    fromFile = new File(params.basedirFile, from.replace('/', File.separator))
	  }

	  if (path != null) {
	    fromFile = new File(fromFile, path.replace('/',File.separator))
	  }

	  assert fromFile.exists(), "from file: $fromFile does not exist"
	  
	  return fromFile;
	}
	
		
	protected File getToFile() {
	  assert to !=null, "the to file cannot be null"

	  def toFile = new File(params.outputDir, to.replace('/', File.separator))

	  if (path != null) {
	    toFile = new File(toFile, path.replace('/',File.separator))
	  }

	  toFile.parentFile.mkdirs()
	  
	  return toFile;
	}
	
}