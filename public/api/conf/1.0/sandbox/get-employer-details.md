##### External Test Environment

You will need to create a [test user](https://test-developer.service.hmrc.gov.uk/api-test-user) when using the external test environment.

*Please ensure your parameters are URL encoded.*

##### Available Data

<table>
    <thead>
        <tr>
        <th style="width:37%">Empref Parameters</th>
        <th style="width:63%">Response</th>
        </tr>
    </thead>
    <tbody>
    <tr>
      <td><code class='code--slim'>111%2FAA00001</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>222%2FAA00001</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>222%2FAA00002</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>222%2FAA00003</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>333%2FAA00001</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840%2FMODES17</code></td>
      <td><pre class='code--block'>
{
"_links": {
    "declarations": {
        "href": "/epaye/111%2FAA00001/declarations"
    },
    "employment-check": {
        "href": "/epaye/111%2FAA00001/employed/{nino}"
    },
    "fractions": {
        "href": "/epaye/111%2FAA00001/fractions"
    },
    "self": {
        "href": "/epaye/111%2FAA00001"
    }
},
"empref": "111/AA00001"
}
</pre>
      </td>
    </tr>
    </tbody>
</table>
