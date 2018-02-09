# state decoder server
This decoder server was created by Zhenghao Dong. The geolocation data is provided by Vistar Media.
I have designed three algorithm to parse the geolocation data(lontitude,latitude) to state name in USA.
In this semester, I am studying nodejs+express. So I use them as the web server framework.

## Structure of web server nodejs(v6.11.4)
- .idea
- bin
- node_mudules
- public
- routes
  - index.js (router code,which use to deal GET/POST)
- tools
  - [GeoDecoder.js](#Algorithm Detail) 
  - states.json (I revise it as a JsonArray,it's easy to tranverse)
- view
- app.js (bootstrap file)
- package.json
- state-decoder-server.iml

## Start conmand
```sh
1. cd state-decoder-server
2. node app.js &

3. curl  -d "longitude=-77.036133&latitude=40.513799" http://localhost:8080/             (use ray algorithm)
   or
   curl  -d "longitude=-77.036133&latitude=40.513799&version=v2" http://localhost:8080/  (use winding number algorithm)
```

## Alogrithm Theory
### Ray Algorithm (geoDecodeV1)
theory: test how many times a ray, starting from the test point and going in any fixed direction,
 *intersects the edges of the polygon. if the test point is in the polygon, it must intersects with polygon even times.
