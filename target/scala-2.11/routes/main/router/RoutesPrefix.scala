
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/Taylor/Programs/Beacon/BeaconServer-0.0.2/conf/routes
// @DATE:Sun Jun 12 20:15:50 EDT 2016


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
