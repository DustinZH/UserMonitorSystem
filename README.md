# state decoder server
This decoder server was created by Zhenghao Dong. The geolocation data is provided by Vistar Media.
I have designed three algorithm to parse the geolocation data(lontitude,latitude) to state name in USA.
In this semester, I am studying nodejs+express. So I use them as the web server framework.

## Structure of web server
- .idea
- bin
- node_mudules
- public
- routes
  - index.js (router code,which use to deal GET/POST)
- tools
  - GeoDecoder.js (The algorithm code)
  - states.json (I revise it as a JsonArray,it's easy to tranverse)
- view
- app.js (bootstrap file)
- package.json
- state-decoder-server.iml
