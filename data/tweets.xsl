<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<table class="conversationslist">
			<tr>
				<th>Tweets per classification</th>
			</tr>
			<xsl:for-each select="tweets/tweet">
				<tr>
					<td>

						<ul class="cclist">
							<li>
								<b>
									<xsl:value-of select="user" />
									:
								</b>

								<xsl:value-of select="text" />
							</li>
						</ul>
					</td>
				</tr>


			</xsl:for-each>
		</table>

	</xsl:template>
</xsl:stylesheet>
