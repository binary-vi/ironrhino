<#ftl output_format='HTML'>
<!DOCTYPE html>
<html>
<head>
<title>${getText('create')}${getText('authorization')}</title>
</head>
<body>
<@s.form action="${actionBaseUrl}/create" method="post" class="ajax reset form-horizontal">
	<div class="control-group listpick" data-options="{'url':'<@url value="/oauth/client/pick?columns=name"/>'}">
	<@s.hidden name="authorization.client" class="listpick-id"/>
	<label class="control-label" for="client">${getText('client')}</label>
	<div class="controls">
	<span class="listpick-name"></span>
	</div>
	</div>
	<div class="control-group listpick" data-options="{'url':'<@url value="/user/pick?columns=username,name&enabled=true"/>','idindex':1}">
	<@s.hidden name="authorization.grantor" class="required listpick-id"/>
	<label class="control-label" for="grantor">${getText('grantor')}</label>
	<div class="controls">
	<span class="listpick-name"></span>
	</div>
	</div>
	<@s.textfield name="authorization.lifetime" value="0" class="required span1"/>
	<@s.textfield name="authorization.scope" class="span4"/>
	<@s.submit label=getText('create') class="btn-primary"/>
</@s.form>
</body>
</html>