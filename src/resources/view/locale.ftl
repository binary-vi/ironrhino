<#ftl output_format='HTML'>
<!DOCTYPE html>
<html>
<head>
<title>${getText('locale')}</title>
</head>
<body>
<form action="${actionBaseUrl}">
<@s.select theme="simple" name="lang" onchange="this.form.submit()" list="availableLocales" listValue="top.getDisplayName(top)" headerKey="" headerValue=""/>	
</form>
</body>
</html>


