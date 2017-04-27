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
      <td><code class='code--slim'>123/AB12345</code></td>
      <td><pre class='code--block'>
{
"declarations": [
    {
        "dateCeased": "2016-09-05",
        "id": 123456843,
        "submissionId": 12345684,
        "submissionTime": "2016-10-15T16:05:23.000"
    },
    {
        "id": 123456831,
        "inactiveFrom": "2016-06-06",
        "inactiveTo": "2016-09-05",
        "submissionId": 12345683,
        "submissionTime": "2016-07-15T16:05:23.000"
    },
    {
        "id": 123456782,
        "levyAllowanceForFullYear": 15000,
        "levyDueYTD": 600,
        "payrollPeriod": {
            "month": 11,
            "year": "16-17"
        },
        "submissionId": 12345678,
        "submissionTime": "2016-07-14T16:05:23.000"
    },
    {
        "id": 123456822,
        "levyAllowanceForFullYear": 15000,
        "levyDueYTD": 200,
        "payrollPeriod": {
            "month": 2,
            "year": "16-17"
        },
        "submissionId": 12345682,
        "submissionTime": "2016-06-15T16:20:23.000"
    },
    {
        "id": 123456812,
        "levyAllowanceForFullYear": 15000,
        "levyDueYTD": 1000,
        "payrollPeriod": {
            "month": 2,
            "year": "16-17"
        },
        "submissionId": 12345681,
        "submissionTime": "2016-06-07T16:05:23.000"
    },
    {
        "id": 123456802,
        "levyAllowanceForFullYear": 15000,
        "levyDueYTD": 500,
        "payrollPeriod": {
            "month": 1,
            "year": "16-17"
        },
        "submissionId": 12345680,
        "submissionTime": "2016-05-07T16:05:23.000"
    },
    {
        "id": 123456790,
        "noPaymentForPeriod": true,
        "payrollPeriod": {
            "month": 12,
            "year": "15-16"
        },
        "submissionId": 12345679,
        "submissionTime": "2015-04-07T16:05:23.000"
    }
],
"empref": "123/AB12345"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840/FZ00065</code></td>
      <td><pre class='code--block'>
{
"declarations": [
    {
        "id": 924352,
        "levyAllowanceForFullYear": 1000,
        "levyDueYTD": 700,
        "payrollPeriod": {
            "month": 1,
            "year": "17-18"
        },
        "submissionId": 92435,
        "submissionTime": "2017-04-20T11:30:50.000"
    },
    {
        "id": 552661,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-09-05",
        "submissionId": 55266,
        "submissionTime": "2014-12-05T15:16:26.000"
    },
    {
        "id": 552651,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-08-05",
        "submissionId": 55265,
        "submissionTime": "2014-12-05T15:16:24.000"
    },
    {
        "id": 552631,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-09-05",
        "submissionId": 55263,
        "submissionTime": "2014-12-05T15:16:23.000"
    },
    {
        "id": 552640,
        "noPaymentForPeriod": true,
        "payrollPeriod": {
            "month": 6,
            "year": "14-15"
        },
        "submissionId": 55264,
        "submissionTime": "2014-12-05T15:16:23.000"
    },
    {
        "id": 552641,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-08-05",
        "submissionId": 55264,
        "submissionTime": "2014-12-05T15:16:23.000"
    },
    {
        "dateCeased": "2014-06-10",
        "id": 552643,
        "submissionId": 55264,
        "submissionTime": "2014-12-05T15:16:23.000"
    },
    {
        "id": 552621,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-08-05",
        "submissionId": 55262,
        "submissionTime": "2014-12-05T15:16:21.000"
    },
    {
        "id": 552601,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-09-05",
        "submissionId": 55260,
        "submissionTime": "2014-12-05T15:16:19.000"
    },
    {
        "id": 552611,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-09-05",
        "submissionId": 55261,
        "submissionTime": "2014-12-05T15:16:19.000"
    },
    {
        "id": 552590,
        "noPaymentForPeriod": true,
        "payrollPeriod": {
            "month": 6,
            "year": "14-15"
        },
        "submissionId": 55259,
        "submissionTime": "2014-12-05T14:54:26.000"
    },
    {
        "id": 552591,
        "inactiveFrom": "2014-12-06",
        "inactiveTo": "2015-08-05",
        "submissionId": 55259,
        "submissionTime": "2014-12-05T14:54:26.000"
    },
    {
        "dateCeased": "2014-05-05",
        "id": 552593,
        "submissionId": 55259,
        "submissionTime": "2014-12-05T14:54:26.000"
    },
    {
        "dateCeased": "2013-04-06",
        "id": 884433,
        "submissionId": 88443,
        "submissionTime": "2013-04-06T12:22:33.000"
    },
    {
        "dateCeased": "2013-04-06",
        "id": 884703,
        "submissionId": 88470,
        "submissionTime": "2013-04-06T12:22:33.000"
    }
],
"empref": "840/FZ00065"
}
</pre>
      </td>
    </tr>
    <tr>
      <td><code class='code--slim'>840/UZ00038</code></td>
      <td><pre class='code--block'>
{
"declarations": [
    {
        "dateCeased": "2014-04-06",
        "id": 294423,
        "submissionId": 29442,
        "submissionTime": "2014-04-28T12:22:33.000"
    }
],
"empref": "840/UZ00038"
}
</pre>
      </td>
    </tr>
    </tbody>
</table>
