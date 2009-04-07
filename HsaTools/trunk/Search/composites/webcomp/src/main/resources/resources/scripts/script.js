/*
 * Copyright 2009 Västa Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 */
/* Used for help popup */
function popup(url) {
	newwindow=window.open(url,'name','scrollbars=1,height=500,width=500');
	if (window.focus) {
		newwindow.focus();
	}
	return false;
}


/* Searching by vgr id */
function radioVgrid() {
}

function changeStyleClass(container, classname) {
    container.className = classname;
}

function attachEvent() {
    function fnCallbackOver(e, obj) {
        changeStyleClass(obj, 'selected-item');
    }
    function fnCallbackOut(e, obj) {
        changeStyleClass(obj, 'normal');
    }

    // array can contain object references, element ids, or both
    var oMainElement = document.getElementById("search-result-container");
    var arrChildren = oMainElement.getElementsByTagName('li');
    var oCurrent;

    for (var i = 0, l = arrChildren.length; i < l; i++) {
        oCurrent  = arrChildren[i];
        YAHOO.util.Event.addListener(oCurrent, "mouseover", fnCallbackOver, oCurrent);
        YAHOO.util.Event.addListener(oCurrent, "mouseout", fnCallbackOut, oCurrent);
    }
}

/* Create VGR namespace if it doesn't already exist */
if (typeof VGR === "undefined") { var VGR = {}; }

/* Check for browser support. */
VGR.browserOK = document.getElementById && document.createTextNode;

/**
 * Turn a specific set of elements into a tab widget.
 * @requires YUI
 */
VGR.KIVSearch = function() {
	var YUD = YAHOO.util.Dom;
	var config = {
		sContId:'kiv-results',
		sHiddenClass:'hidden',
		sTabClass:'tab',
		sTabElement:'div',
		sTabHeaderElement:'h3',
		sTabContentClass:'tab-bd',
		sTabContentElement:'div'
	};
	function init() {
		if (!VGR.browserOK) { return; }
		var oCont = YUD.get(config.sContId);
		if (!oCont) { return; }
		// Add tabs
		var tabView = new YAHOO.widget.TabView();
		var modules = YUD.getElementsByClassName(config.sTabClass, config.sTabElement, oCont);
		YUD.batch(modules, function(module) {
			tabView.addTab(new YAHOO.widget.Tab({
				label: module.getElementsByTagName(config.sTabHeaderElement)[0].innerHTML,
				contentEl: YUD.getElementsByClassName(config.sTabContentClass, config.sTabContentElement, module)[0]
			}));
			// Hide modules
			YUD.addClass(module, config.sHiddenClass);
		});
		// Make the first tab active
		tabView.set('activeIndex', 0);
		tabView.appendTo(oCont);
	}
	return {
		init:init
	};
}();

YAHOO.util.Event.onDOMReady(function () {
	VGR.KIVSearch.init();
    attachEvent();
});