Please ensure parameters are URL encoded.

<table>
    <thead>
        <tr>
            <th style="width:63%" colspan="4">Parameters</th>
            <th style="width:37%">Response</th>
        </tr>
        <tr>
            <th style="width:20%">Empref</th>
            <th style="width:25%">NI</th>
            <th style="width:22.5%">From Date</th>
            <th style="width:22.5%">To Date</th>
            <th>&nbsp;</th>
        </tr>
    </thead>
    <tbody>
    <tr>
      <td><code class='code--slim'>123%2FAB12345</code></td>
      <td><code class='code--slim'>SC111111A</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": true,
"empref": "123/AB12345",
"fromDate": "2010-01-01",
"nino": "SC111111A",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840%2FMODES17</code></td>
      <td><code class='code--slim'>SC111111A</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": true,
"empref": "840/MODES17",
"fromDate": "2010-01-01",
"nino": "SC111111A",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840%2FMODES17</code></td>
      <td><code class='code--slim'>AA123456C</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": false,
"empref": "840/MODES17",
"fromDate": "2010-01-01",
"nino": "AA123456C",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>111%2FAA00001</code></td>
      <td><code class='code--slim'>AA123456C</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": false,
"empref": "111/AA00001",
"fromDate": "2010-01-01",
"nino": "AA123456C",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840%2FHZ00064</code></td>
      <td><code class='code--slim'>AS960509A</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": true,
"empref": "840/HZ00064",
"fromDate": "2010-01-01",
"nino": "AS960509A",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>923%2FEZ00059</code></td>
      <td><code class='code--slim'>PR555555A</code></td>
      <td><code class='code--slim'>2010-01-01</code></td>
      <td><code class='code--slim'>2018-01-01</code></td>
      <td><pre class='code--block'>
{
"employed": false,
"empref": "923/EZ00059",
"fromDate": "2010-01-01",
"nino": "PR555555A",
"toDate": "2018-01-01"
}
</pre>
      </td>
    </tr>
    </tbody>
</table>
