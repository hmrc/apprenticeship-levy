##### External Test Environment

If accessing this endpoint via the new [external test environment](https://test-developer.service.hmrc.gov.uk/api-documentation) unfortunately
the generated test user account can not be updated with replacement or additional
EPAYE employer references numbers and so no test data can be returned. In this
environment it's important to include an additional
<code style="code--slim">OVERRIDE_EMPREF</code> header with any requests and where the value is an empref (**not** URL encoded) e.g. 840/MODES17. Valid values are found in the table below. The URL's empref in this case must match the one associated with your uniquely generated test user's empref provided during the
[test user creation](https://test-developer.service.hmrc.gov.uk/api-test-user). Please note this header is ignored in the production environment live endpoints.

If using the original Sandbox environment please ensure your parameters are URL encoded.

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
