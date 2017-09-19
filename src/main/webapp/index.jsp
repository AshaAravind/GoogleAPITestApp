<!DOCTYPE link PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<link href="css/bootstrap-datetimepicker.css" rel="stylesheet" type="text/css" />
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/bootstrap-datetimepicker.js"></script>
<script type="text/javascript" src="js/app.js"></script>
</head>
<body>
<div class="container">
<div class="success-message" hidden>
Successfully added the event.
</div>
<form role="form" id="calendar-form">
<div class="form-group">
<label for="summary">Event Summary</label>
<input required type="text" class="form-control" id="summary" name="summary" placeholder="Enter event summary">
</div>
<div class="form-group">
<label for="description">Event Description</label>
<input type="text" class="form-control" id="description" name="description" placeholder="Enter event Description">
</div>
<div class="form-group">
<label for="location">Event Location</label>
<input type="text" class="form-control" id="location" name="location" placeholder="Enter location">
</div>
<div class="form-group">
<label for="stDate">Event Start Date</label>
<div class="container">
    <div class="row">
        <div class='col-sm-6'>
            <div class="form-group">
                <div class='input-group date' id='datetimepicker1'>
                    <input required type='text' name="stDate" class="form-control" />
                    <span class="input-group-addon">
                        <span class="glyphicon glyphicon-calendar"></span>
                    </span>
                </div>
            </div>
        </div>
        <script type="text/javascript">
            $(function () {
                $('#datetimepicker1').datetimepicker();
            });
        </script>
    </div>
</div>
</div>
<div class="form-group">
<label for="endDate">Event End Date</label>
<div class="container">
    <div class="row">
        <div class='col-sm-6'>
            <div class="form-group">
                <div class='input-group date' id='datetimepicker2'>
                    <input required type='text' name="endDate" class="form-control" />
                    <span class="input-group-addon">
                        <span class="glyphicon glyphicon-calendar"></span>
                    </span>
                </div>
            </div>
        </div>
        <script type="text/javascript">
            $(function () {
                $('#datetimepicker2').datetimepicker();
            });
        </script>
    </div>
</div>
</div>
<button type="submit" class="btn btn-default form-submit">Submit</button>
</form>
<!-- Modal -->
<div class="modal fade" id="authFailedModal" tabindex="-1" role="dialog" aria-labelledby="authFailedModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title" id="myModalLabel">Authentication required</h4>
      </div>
      <div class="modal-body">
        Please authorize the app to add event to your google calendar
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary login">Sign in with Google</button>
      </div>
    </div>
  </div>
</div>
</div>
</body>
</html>
