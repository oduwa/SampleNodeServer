var express = require('express');
var app = express();

app.set('port', (process.env.PORT || 5000));

app.get('/', function (req, res) {
  res.send('Hello World from odie!');
});

app.listen(app.get('port'), function () {
  console.log('Example app listening on port 3000!');
});