/**
 * @file No ADB for CKEditor
 * Copyright (C) 2014 Alfonso Mart�nez de Lizarrondo
 * Removes the abp attribute inserted by the AdBlock Plus extension for IE11
 * Version 1.1
 */
(function() {
"use strict";

CKEDITOR.plugins.add( 'noabp',
{
	init : function( editor )
	{
	},

	afterInit : function( editor )
	{
		editor.on('instanceReady' , function() {
			// Install our filter only if we detect that the AdBlock Plus is inserting its garbage
			if (!editor.editable().$.getAttribute('abp'))
				return;

			// htmlFilter : conversion from internal data to html output.
			editor.dataProcessor.htmlFilter.addRules(
			{
				elements :
				{
					$ : function( element ) {
						// remove ABP attributes
						var attributes = element.attributes,
							abp = attributes && attributes.abp;

						if (abp)
							delete element.attributes.abp;
					},
					form : function( element ) {
						// remove additional attribute from forms
						var attributes = element.attributes,
							abframeid = attributes && attributes.abframeid;

						if (abframeid)
							delete element.attributes.abframeid;
					}
				}
			}, {applyToAll :true});
		});
	}

} );

})();
