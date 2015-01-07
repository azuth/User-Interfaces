$.ajaxSetup({
	cache: false,
	statusCode: {
		401: function(){
			$('#warningInfo').fadeIn(300);
			$('#warningInfo div div').empty().append(				
					"<strong>No Authentication.</strong> "
					+"Please  <a href=\"#login\">sign in</a>. "
					+"Not a member yet? <a href=\"#register\">Register Now!</a>");
		},
		201: function(){
			alert("created User");
		},
		403: function(){
			
		},
		404: function(){
			alert("Username is already registered\nChoose another.");
		},
		409: function(){
			alert("Username is already registered\nChoose another.");
		}
	}
});

$(document).ajaxError(function(){
	  //alert("An error occurred!");
	}); 


function dumpData(data) {
	 $.each(data, function(key, val) {
		 $("#dump").append("id = " + key + ", value = " + val + "<br/>\n");
	 });
	 $("#dump").append("----<br/>\n");
}

/* Setzt den Header fuer die Basic-Athentifizierung. */
function login(name, password) {
	$.ajaxSetup({
		  headers: {
		    'Authorization': "Basic " + btoa(name + ":" + password)
		  }
		});
}

/* checking login*/
function checkAuthorization(name) {
	$.getJSON('/PictureCommunity/REST/user/friends/' + name)
		.fail(function(jqxhr, textStatus, error) {
			//$("#dump").append(error + "<br/>\n");
			console.log(error);
		})
		.done(function(data) {
			//$("#dump").append("valid user and pw" + "<br/>\n");
			console.log("valid user and pw");
		});


	;
}

/* Setzt den Header fuer die Basic-Athentifizierung auf einen ungueltigen Wert. */
function logout() {
	$.ajaxSetup({
		  headers: {
		    'Authorization': "Basic " + btoa('' + ":" + '')
		  }
		});
}

/* Einen neuen Anwender anmelden */
function register(name, password, mail) {
	var senddata={ username : name, password : password, mailaddress: mail };
	$.post('/PictureCommunity/REST/register', senddata, function(data) {
		dumpData(data);
	});
}

/* Sucht den Anwender, dessen Anmeldenamen die Zeichenkette name enthaelt. */
function findUsers(name) {
	$.getJSON('/PictureCommunity/REST/user/name/' + name, {}, function(data) {
		$.each(data, function(key, val) {
			dumpData(val);
		});
	});
}

/* Sucht den Anwender mit der uebergebenen Mailadresse. */
function findUser(mailAddress) {
	$.getJSON('/PictureCommunity/REST/user/mailaddress/' + mailAddress, {}, function(data) {
		dumpData(data);
	});
}

/* Ermittelt alle Freunde des Anwenders mit dem uebergebenen vollstaendigen Anmeldenamen. */
function friendsOf(name) {
	$.getJSON('/PictureCommunity/REST/user/friends/' + name, {}, function(data) {
		$.each(data, function(key, val) {
			dumpData(val);
		});
	});
}

/* Ermittelt die Informationen zu allen Bildern, die fuer den aktuellen Anwender sichtbar sind. */
function getPictureInfos(name,callback) {
	$.getJSON('/PictureCommunity/REST/pictures/' + name,{})
		.fail(function(jqxhr, textStatus, error) {
			$("#dump").append(error + "<br/>\n");
		})
		.done(function(data){
			callback(data);
		});
}

/* Laedt ein Bild mit der uebergebenen ID und traegt es in das div-Tag mit der ID iamges ein. */
/* Durch die Angabe eines gueltigen Breite wird das Bild auf diese Groesse gestreckt oder gestaucht. */
/* Hat der Benutzer nicht vorher den Header fuer die Basic-Athentifizierung gesetzt, dann werden nur die oeffentlichen Bilder */
/* beruecksichtigt. */
function loadPicture(parentNode,id, width,classattr) {
	var img = document.createElement('img');
	img.onload = function() {
		parentNode.append(img);
	}
	img.onerror = function() {
		console.log("no Authorization");
	}
	
	
	
	if (width >= 1 && width < 10000) {
		img.src = '/PictureCommunity/REST/picture/' + id + '?width=' + width + '&_timestamp=' + Math.random();

	}
	else {
		img.src = '/PictureCommunity/REST/picture/' + id + '?_timestamp=' + Math.random();
	}

	if(classattr!= null){
		img.setAttribute("class", classattr);
	}
}

$(function () {
    $('#fileupload').fileupload({url: '/PictureCommunity/REST/newpicture', formData: function(form) {
		return [{ name: 'description', value: 'Ein perfektes Bild eines Meisters'}, { name: 'public', value: true }]; 
	}});
});