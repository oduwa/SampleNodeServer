/* Find cron docs here - https://www.npmjs.com/package/cron */
var jobCounter = 0;

var express = require('express');
var app = express();

var CronJob = require('cron').CronJob;
var job = new CronJob({
  cronTime: '*/1 * * * *',
  onTick: function() {
      jobCounter++;
      app.set('counter', jobCounter);
      console.log("Ran job for the " + jobCounter + "th time");
  },
  start: false,
});

app.set('port', (process.env.PORT || 5000));

app.get('/', function (req, res) {
  res.send('Hello World from odie!\n' + 'Has run job ' + app.get('counter') + ' times');
});

app.listen(app.get('port'), function () {
  console.log('Example app listening on port 5000!');
});

//job.start();