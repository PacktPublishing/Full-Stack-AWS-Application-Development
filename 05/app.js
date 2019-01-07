var receiptBucketName = 'aws-packt-receipt-files';
var bucketRegion = 'us-west-2';
var IdentityPoolId = 'us-west-2:cc4ad9e6-b350-48b5-84f5-bb6ffd7f25cb';
var clientId1 = '4kq8h1580c9c3ekjp9us0v6g22';
var clientId2 = '4384igoa6uva4q8tb28uq2ofmt';
var UserPoolId = 'us-west-2_CFOBoF7ch';

AWS.config.update({
  region: bucketRegion,
  credentials: new AWS.CognitoIdentityCredentials({
    IdentityPoolId: IdentityPoolId
  })
});

AWSCognito.config.region = bucketRegion;

var poolData = {
    UserPoolId : UserPoolId, // your user pool id here
    ClientId : clientId2 // your app client id here
};

console.log('URL: ' + window.location);

var token = window.location.hash;
console.log('Param: ' + token);
if ( token != "" ) {
	console.log("Found code!");
}
else {
	alert('User not logged in!');
	window.location.href='https://packt-videos.auth.us-west-2.amazoncognito.com/signup?response_type=token&client_id='+clientId2+'&redirect_uri=https://d3co69sirvxtpl.cloudfront.net/index.html';
}

token = token.substring(10);
amp = token.indexOf("&");
token = token.substring(0,amp);
var decoded = parseJwt(token);
console.log("Token: " + token);
console.log("Decoded: " + decoded);
var username = decoded['cognito:username'];

//for(var pname in decoded){
//	console.log('Name: ' + pname + " -> " + decoded[pname]);
//}

AWS.config.credentials = new AWS.CognitoIdentityCredentials({
	IdentityPoolId: 'us-west-2:cc4ad9e6-b350-48b5-84f5-bb6ffd7f25cb',
	Logins: {
		'cognito-idp.us-west-2.amazonaws.com/us-west-2_CFOBoF7ch': token
	}
});

AWS.config.credentials.get(function(error){
	console.log(error);
    // Credentials will be available when this function is called.
    var accessKeyId = AWS.config.credentials.accessKeyId;
    var secretAccessKey = AWS.config.credentials.secretAccessKey;
    var sessionToken = AWS.config.credentials.sessionToken;
    
    console.log('AccessKeyId: ' + accessKeyId);
    
    var identityId = AWS.config.credentials.identityId;
    console.log('IdentityId: ' + identityId);
});

var s3 = new AWS.S3({
	  apiVersion: '2006-03-01',
	  params: {Bucket: receiptBucketName}
	});


function listFolders() {
	  s3.listObjects({Delimiter: '/', Prefix: username}, function(err, data) {
	    if (err) {
	      return alert('There was an error listing your receipts: ' + err.message);
	    } else {
	      var folders = data.CommonPrefixes.map(function(commonPrefix) {
	        var prefix = commonPrefix.Prefix;
	        var folderName = decodeURIComponent(prefix.replace('/', ''));
	        var folderNameNoUsername = folderName.substring(folderName.indexOf('-')+1);
	        return getHtml([
	          '<li>',
	            '<span onclick="deleteFolder(\'' + folderName + '\')">X</span>',
	            '<span onclick="viewFolder(\'' + folderName + '\')">',
	              folderNameNoUsername,
	            '</span>',
	          '</li>'
	        ]);
	      });
	      var message = folders.length ?
	        getHtml([
	          '<p>Click on an folder name to view it.</p>',
	          '<p>Click on the X to delete the folder.</p>'
	        ]) :
	        '<p>You do not have any folders. Please Create folder.';
	      var htmlTemplate = [
	        '<h2>Folders</h2>',
	        message,
	        '<ul>',
	          getHtml(folders),
	        '</ul>',
	        '<button onclick="createFolder(prompt(\'Enter Folder Name:\'))">',
	          'Create New Folder',
	        '</button>'
	      ]
	      document.getElementById('app').innerHTML = getHtml(htmlTemplate);
	    }
	  });
	}

function createFolder(folderName) {
	  folderName = folderName.trim();
	  if (!folderName) {
	    return alert('Folder names must contain at least one non-space character.');
	  }
	  if (folderName.indexOf('/') !== -1) {
	    return alert('Folder names cannot contain slashes.');
	  }
	  var folderKey = encodeURIComponent(username + '-' + folderName) + '/';
	  s3.headObject({Key: folderKey}, function(err, data) {
	    if (!err) {
	      return alert('Folder already exists.');
	    }
	    if (err.code !== 'NotFound') {
	      return alert('There was an error creating your folder: ' + err.message);
	    }
	    s3.putObject({Key: folderKey}, function(err, data) {
	      if (err) {
	        return alert('There was an error creating your folder: ' + err.message);
	      }
	      alert('Successfully created folder.');
	      viewFolder(folderName);
	    });
	  });
	}

function viewFolder(folderName) {
	  var folderReceiptsKey = encodeURIComponent(folderName) + '//';
      var folderNameNoUsername = folderName.substring(folderName.indexOf('-')+1);
	  s3.listObjects({Prefix: folderReceiptsKey}, function(err, data) {
	    if (err) {
	      return alert('There was an error viewing your folder: ' + err.message);
	    }
	    // `this` references the AWS.Response instance that represents the response
	    var href = this.request.httpRequest.endpoint.href;
	    var bucketUrl = href + receiptBucketName + '/';

	    var receipts = data.Contents.map(function(receipt) {
	      var receiptKey = receipt.Key;
	      var receiptUrl = bucketUrl + encodeURIComponent(receiptKey);
	      return getHtml([
	        '<span>',
	          '<div>',
	            '<img style="width:128px;height:128px;" src="' + receiptUrl + '"/>',
	          '</div>',
	          '<div>',
	            '<span onclick="deleteReceipt(\'' + folderName + "','" + receiptKey + '\')">',
	              'X',
	            '</span>',
	            '<span>',
	              receiptKey.replace(folderReceiptsKey, ''),
	            '</span>',
	          '</div>',
	        '<span>',
	      ]);
	    });
	    var receiptDataHtml = '<br/>Amount: <input id="amount"/>';
	    receiptDataHtml += '<br/>Reason: <input id="reason"/>';
	    receiptDataHtml += '<br/>Description: <input id="description"/>';
	    var message = receipts.length ?
	      '<p>Click on the X to delete the receipt</p>' :
	      '<p>You do not have any receipts in this folder. Please add receipts.</p>';
	    var htmlTemplate = [
	      '<h2>',
	        'Folder: ' + folderNameNoUsername,
	      '</h2>',
	      message,
	      '<div>',
	        getHtml(receipts),
	      '</div>',
	      '<input id="receiptupload" type="file" accept="image/*">' + receiptDataHtml,
	      '<button id="addreceipt" onclick="addReceipt(\'' + folderName +'\')">',
	        'Add Receipt',
	      '</button>',
	      '<button onclick="listFolders()">',
	        'Back To Folders',
	      '</button>',      
	    ]
	    document.getElementById('app').innerHTML = getHtml(htmlTemplate);
	  });
	}

function addReceipt(folderName) {
	  var files = document.getElementById('receiptupload').files;
	  if (!files.length) {
	    return alert('Please choose a file to upload first.');
	  }
	  var file = files[0];
	  var fileName = file.name;
	  var folderReceiptsKey = encodeURIComponent(folderName) + '//';
	  var amount = document.getElementById('amount').value;
	  var reason = document.getElementById('reason').value;
	  var description = document.getElementById('description').value;

	  // We'll first upload the receipt, then call the API to add meta-data
	  var receiptKey = folderReceiptsKey + fileName;
	  s3.upload({
	    Key: receiptKey,
	    Body: file,
	    ACL: 'public-read'
	  }, function(err, data) {
	    if (err) {
	      return alert('There was an error uploading your receipt: ', err.message);
	    }

	    alert('Successfully uploaded receipt.');

	    // Call API here
	    saveReceiptData(receiptKey, amount, reason, description);
	    
	    viewFolder(folderName);
	  });
	}

function deleteReceipt(folderName, receiptKey) {
	  s3.deleteObject({Key: receiptKey}, function(err, data) {
	    if (err) {
	      return alert('There was an error deleting your receipt: ', err.message);
	    }
	    alert('Successfully deleted receipt.');
	    viewFolder(folderName);
	  });
	}

function deleteFolder(folderName) {
	  var folderKey = encodeURIComponent(folderName) + '/';
	  s3.listObjects({Prefix: folderKey}, function(err, data) {
	    if (err) {
	      return alert('There was an error deleting your folder: ', err.message);
	    }
	    var objects = data.Contents.map(function(object) {
	      return {Key: object.Key};
	    });
	    s3.deleteObjects({
	      Delete: {Objects: objects, Quiet: true}
	    }, function(err, data) {
	      if (err) {
	        return alert('There was an error deleting your folder: ', err.message);
	      }
	      alert('Successfully deleted folder.');
	      listFolders();
	    });
	  });
	}

function getReceiptData(id)
{
	console.log('In getReceiptData');
    URL = "https://xo758eybja.execute-api.us-west-2.amazonaws.com/dev/receipts";  //Your URL
	
	$.ajax({
		type: "GET",
		url: URL,
		crossDomain: true
	}).then(function(data) {
		console.log(data.description);
	});
}

function saveReceiptData(receiptKey, amount, reason, description)
{
	console.log('In saveReceiptData');

    var ItemJSON = {};
    
    ItemJSON['id'] = receiptKey;
    ItemJSON['amount'] = amount;
    ItemJSON['reason'] = reason;
    ItemJSON['description'] = description;

    URL = "https://xo758eybja.execute-api.us-west-2.amazonaws.com/dev/receipts";  //Your URL
    
    $.ajax({
    	url: URL,
    	type: "POST",
    	dataType: "json",
    	data: JSON.stringify(ItemJSON),
    	contentType: "application/json",
    	crossDomain: true
    }).then(function(data) {
    	$('.lambdaStatus').append(data.id);
    });
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i <ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}
function parseJwt (token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace('-', '+').replace('_', '/');
    return JSON.parse(window.atob(base64));
};
function getUrlParameter(name) {
    name = name.replace(/[\[]/, '\\[').replace(/[\]]/, '\\]');
    var regex = new RegExp('[\\?&]' + name + '=([^&#]*)');
    var results = regex.exec(location.search);
    return results === null ? '' : decodeURIComponent(results[1].replace(/\+/g, ' '));
}