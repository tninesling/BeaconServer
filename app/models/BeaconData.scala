package models

case class BeaconData(
  creator: String,
  //location: Point,
  latitude: Double = 0.0,
  longitude: Double = 0.0,
  title: String = "",
  address: String = "",
  venueName: String = "",
  startTime: Date = new Date,
  endTime: Date = new Date,
  range: Double = 0.1, // range in miles
  tags: List[String] = List()
) {
  require(!endTime.before(startTime))
}
