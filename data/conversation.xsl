<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
		<table class="conversationslist">
			<tr>
				<th>Conversations</th>
				<th>Entities List</th>
			</tr>
			<xsl:for-each select="datesummary/conversation">
				<xsl:sort select="entities/ConvoScore" order="descending" data-type="number"/>
				<tr>
					<td>
						<table class="conversation">
							<xsl:for-each select="tweet">
								<tr>
									<td>
										<ul class="cclist">
											<li>
												<b>
													<xsl:value-of select="tweetUser" />
													:
												</b>

												<xsl:value-of select="text" />
											</li>
										</ul>
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</td>
					<td>
						<table class="conversation">
							<xsl:if test="entities/ConvoScore">
								<tr>
									<td>
										<b>Conversation Score:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/ConvoScore">
								<tr>
									<td>
										&#160;&#160;
										<xsl:value-of select="." />
									</td>
								</tr>
							</xsl:for-each>

							<xsl:if test="entities/user">
								<tr>
									<td>
										<b>Users:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/user">
								<tr>
									<td>
										&#160;&#160;
										<xsl:value-of select="." />
									</td>
								</tr>
							</xsl:for-each>
							<xsl:if test="entities/hashtags">
								<tr>
									<td>
										<b>Hashtags:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/hashtags">
								<tr>
									<td>
										&#160;&#160;
										<xsl:value-of select="." />
									</td>
								</tr>
							</xsl:for-each>
							<xsl:if test="entities/topic">
								<tr>
									<td>
										<b>Suggested Topics:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/topic">
								<tr>
									<td>
										&#160;&#160;
										<xsl:value-of select="." />
									</td>
								</tr>
							</xsl:for-each>
							
							<xsl:if test="entities/url">
								<tr>
									<td>
										<b>URLs:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/url">
								<tr>
									<td>
										&#160;&#160;
										<xsl:variable name="urlObtained"><xsl:value-of select="."/></xsl:variable>
										<a href="{$urlObtained}"><xsl:value-of select="."/></a>
									</td>
								</tr>
							</xsl:for-each>
							<xsl:if test="entities/media">
								<tr>
									<td>
										<b>Media:</b>
									</td>
								</tr>
							</xsl:if>
							<xsl:for-each select="entities/media">
								<tr>
									<td>
										&#160;&#160;
										<xsl:value-of select="." />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<hr />
					</td>
					<td>
						<hr />
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<br />

	</xsl:template>
</xsl:stylesheet>
