
content = ""

function u(text) { return "<u>" + text + "</u>" }
function m(text) { return markdown.toHTML(text) }
function f() { return sprintf.apply(this, arguments) }
function pr(text) { content += text }

function pru(text) { pr(u(text)) }
function prm(text) { pr(m(text)) }
function prf() { pr(f.apply(this, arguments)) }
function prmf() { pr(m(f.apply(this, arguments))) }
function prumf() { pr(u(m(f.apply(this, arguments)))) }

function p(small, big) { return (small * 100.0) / big; }

function genReport() {
    pr('<h1>Income Report</h1>')
    for(var period in income) {
        total_income_for_period = print_paychecks(period, income[period])
        monthly_average_report(total_income_for_period)
    }

    pr('<h1>Donations</h1>')
    giving_2016 = print_giving("Donations in 2016", giving_2016)
    giving_2015 = print_giving("Donations in 2015", giving_2015)

    return content

    pr('<h3>Donation Planning</h3>')
    target_giving = (total_income * rate) / 100

    prmf("Rate of Giving: %f%%", rate)
    prmf("Total Income so far: $%d", total_income)
    prmf("Target Giving: $%.2f", target_giving)
    prmf("Total Commitments: $%.2f", total_commitments_2015)

    nominal_base = 85000

    biweekly_pay_2015 = 3269.23
    biweekly_giving_2015_only = p(biweekly_pay_2015, rate)

    giving_for_2014 = p(total_income_2014['gross'], rate)
    giving_for_2015 = p(nominal_base, rate)

    giving_in_2015 = giving_for_2014 + giving_for_2015
    giving_in_2015_biweekly = giving_in_2015 / 26

    prumf("Giving in 2015")
    prmf("Giving Rate: %d%%, which is __$%f__ of base $%d, or *__$%f__* of biweekly $%f", rate, giving_for_2015, nominal_base, biweekly_giving_2015_only, biweekly_pay_2015)
    prmf("Missed 2014 Giving: %d%% of $%.2f = _$%.2f_", rate, total_income_2014['gross'], giving_for_2014)
    prmf("Total Giving for 2015: $%.2f + $%.2f = __$%.2f__ (i.e. *$%.2f* biweekly).", giving_for_2014, giving_for_2015, giving_in_2015, giving_in_2015_biweekly)

    prumf("Giving -- Total & Remainder")
    committed = total_commitments_2015
    committed_percent = committed * 100 / nominal_base
    committed_per_pay_period = committed / 26
    prmf("Total: __$%.2f__ (*__%.2f%%__* of $%d; _$%.2f_ biweekly)", committed, committed_percent, nominal_base, committed_per_pay_period)
    committed_remainder_annual = giving_in_2015 - committed
    prmf("Annual Remainder: $%.2f - $%.2f = __$%.2f__", giving_in_2015, committed, committed_remainder_annual)
    committed_remainder = giving_in_2015_biweekly - committed_per_pay_period
    prmf("Biweekly Remainder: $%.2f - $%.2f = $_%.2f_", giving_in_2015_biweekly, committed_per_pay_period, committed_remainder)

    prumf("Pending Giving in 2015")
    past_pay_periods_2015 = 11
    remaining_pay_periods = 26 - past_pay_periods_2015
    prmf("Past Pay Periods: %d", past_pay_periods_2015)
    prmf("Remaining Pay Periods: **%d**", remaining_pay_periods)

    total_missed_2015 = committed_remainder * past_pay_periods_2015
    prmf("2015 Pending: $%.2f * %d = $%.2f", committed_remainder, past_pay_periods_2015, total_missed_2015)
    missed_spread_2015 = total_missed_2015 / remaining_pay_periods
    prmf("Spread: $%.2f ÷ %d = $%.2f", total_missed_2015, remaining_pay_periods, missed_spread_2015)
    adjusted_future_biweekly_giving = giving_in_2015_biweekly + missed_spread_2015
    prmf("New Future Biweekly: $%.2f + $%.2f = _$%.2f_", giving_in_2015_biweekly, missed_spread_2015, adjusted_future_biweekly_giving)
    unallocated_future_biweekly_giving = committed_remainder + missed_spread_2015
    prmf("Pending Future Biweekly: $%.2f + $%.2f = __$%.2f__", committed_remainder, missed_spread_2015, unallocated_future_biweekly_giving)

    return content
}

function apply_projection(income_info) {
    // get last paycheck info
    var last_paycheck, last_paycheck_date
    for(var pay_date in income_info.paychecks) {
        last_paycheck_date = moment(pay_date)
        last_paycheck = income_info.paychecks[pay_date]
    }

    current_year = last_paycheck_date.year()
    projected_date = last_paycheck_date.add(2, 'w')
    for(; projected_date.year() === current_year; projected_date = projected_date.add(2, 'w')) {
        formatted_projected_date = projected_date.format('YYYY-MM-DD')
        income_info.paychecks[formatted_projected_date] = _.clone(last_paycheck)
        income_info.paychecks[formatted_projected_date].projection = true
    }
}

function print_paychecks(title, income_info) {
    if(income_info.projection === true) {
        apply_projection(income_info)
    }

    paychecks = income_info.paychecks

    prf('<h2><u>%s</u></h2>', title)
    total = { 'net' : 0, 'gross' : 0, 'x401k' : 0, 'hsa' : 0, 'ftax' : 0, 'sstax' : 0, 'mtax' : 0,
              'stax' : 0, 'ctax' : 0, 'health' : 0, 'metro' : 0, 'other' : 0,
              'first_pay_date' : null, 'last_pay_date' : 0 }
    total['first_pay_date'] = null
    total['last_pay_date'] = null

    pr('<table>')
    pr('<tr> <th>#</th> <th>Pay Date</th> <th>Gross Pay</th> <th>Fed Tax</th> <th>SS Tax</th> \
<th>Md Tax</th> <th>St. Tax</th> <th>Loc Tax</th> <th>Health</th> <th>Metro</th> \
<th>Other</th> <th>401(k)</th> <th>H/FSA</th> <th>Net Pay</th> </tr>')

    i = 0
    for(var pay_date in paychecks) {
        paycheck = paychecks[pay_date]
        pay_date = moment(pay_date)

        const getBound = (otherBound) =>
            (income_info.natural_order ^ otherBound) ? 'last_pay_date' : 'first_pay_date';
        const getOffset = (otherBound) =>
            (income_info.natural_order ^ otherBound) ? 'final_offset' : 'start_offset';

        /*
            0 0 - 0
            0 1 - 1
            1 0 - 1
            1 1 - 0
         */
        if (i === 0)
            total[getBound(false)] = moment(pay_date).subtract(income_info[getOffset(false)], 'days')
        else
            total[getBound(true)] = moment(pay_date).subtract(income_info[getOffset(true)], 'days')

        total['net'] += paycheck['net']
        total['gross'] += paycheck['gross']
        total['x401k'] += paycheck['x401k']
        total['hsa'] += paycheck['hsa']
        total['ftax'] += paycheck['ftax']
        total['sstax'] += paycheck['sstax']
        total['mtax'] += paycheck['mtax']
        total['stax'] += paycheck['stax']
        total['ctax'] += paycheck['ctax']
        total['health'] += paycheck['health']
        total['metro'] += paycheck['metro']
        total['other'] += paycheck['other']

        if(paycheck.projection === true) {
            pr('<tr class="projection">')
        } else {
            pr('<tr>')
        }

        pr("<td>" + ++i + "</td>")
        pr("<td>" + pay_date.format("MM/DD/YY") + "</td>")

        prf("<td>$%(gross).2f</td>", paycheck)
        prf("<td>$%(ftax).2f</td>", paycheck)
        prf("<td>$%(sstax).2f</td>", paycheck)
        prf("<td>$%(mtax).2f</td>", paycheck)
        prf("<td>$%(stax).2f</td>", paycheck)
        prf("<td>$%(ctax).2f</td>", paycheck)
        prf("<td>$%(health).2f</td>", paycheck)
        prf("<td>$%(metro).2f</td>", paycheck)
        prf("<td>$%(other).2f</td>", paycheck)
        prf("<td>$%(x401k).2f</td>", paycheck)
        prf("<td>$%(hsa).2f</td>", paycheck)
        prf("<td style=\"color: blue;\">$%(net).2f</td>", paycheck)

        error_gap = paycheck['gross'] - paycheck['net'] - paycheck['ftax'] - paycheck['sstax']
                - paycheck['mtax'] - paycheck['stax'] - paycheck['ctax'] - paycheck['x401k']
                - paycheck['hsa'] - paycheck['health'] - paycheck['metro'] - paycheck['other']
        error_tolerance = 0.0101 // 1.01 cents
        if ( !(error_gap >= -error_tolerance && error_gap <= error_tolerance) ) {
            prf("<td style=\"color: red;\">$%.2f</td>", error_gap)
        }

        pr("</tr>")
    }
    prf(sprintf('<tr> <th></th> <th><em>Total</em></th> <th>$%(gross).2f</th> <th>$%(ftax).2f</th> \
<th>$%(sstax).2f</th> <th>$%(mtax).2f</th> <th>$%(stax).2f</th> <th>$%(ctax).2f</th> <th>$%(health).2f</th> \
<th>$%(metro).2f</th> <th>$%(other).2f</th> <th>$%(x401k).2f</th> <th>$%(hsa).2f</th> <th>$%(net).2f</th> </tr>', total))

    total_pc = _.object(_.map(total, function (amt, key) { return [ key, Math.round((amt * 10000) / total['gross']) / 100 ]; }));
    prf(sprintf('<tr class="percentages"> <td></td> <td><em>Percent</em></td> <td>%(gross)f&#37;</td> <td>%(ftax)f&#37;</td> \
<td>%(sstax)f&#37;</td> <td>%(mtax)f&#37;</td> <td>%(stax)f&#37;</td> <td>%(ctax)f&#37;</td> <td>%(health)f&#37;</td> \
<td>%(metro)f&#37;</td> <td>%(other)f&#37;</td> <td>%(x401k)f&#37;</td> <td>%(hsa)f&#37;</td> <td>%(net)f&#37;</td> </tr>', total_pc))

    prf('</table>')
    return total
}

function monthly_average_report(total) {
    total_taxes = total['ftax'] + total['sstax'] + total['mtax'] + total['stax'] + total['ctax']
    total_deductions = total['x401k'] + total['hsa'] + total['health'] + total['metro'] + total['other']

    pr('<br>')
    date_format = 'MMM Do, YYYY'
    total_period = total['last_pay_date'].diff(total['first_pay_date'], 'days') + 1
    month_days = 365.25 / 12
    factor = month_days / total_period

    total['first_work_date'] = total['first_pay_date'].format(date_format)
    total['last_work_date'] = total['last_pay_date'].format(date_format)
    monthly_gross_pay = total['gross'] * factor
    monthly_net_pay = total['net'] * factor
    monthyl_total_taxes = total_taxes * factor
    monthyl_deductions = total_deductions * factor

    prf("<em>Work Period</em>: %s <em>to</em> %s (<strong>%d</strong> days)<br><br>",
        total['first_work_date'], total['last_work_date'], total_period)
    prf("Monthly Net Pay: <span style=\"color: blue;\">$%.2f</span> (<strong>%.2f%%</strong> of gross)<br>",
        monthly_net_pay, p(monthly_net_pay, monthly_gross_pay))
    prf("Monthly Taxes: $%.2f (<strong>%.2f%%</strong> of gross)<br>",
        monthyl_total_taxes, p(monthyl_total_taxes, monthly_gross_pay))
    prf("Monthly Deductions: $%.2f (<strong>%.2f%%</strong> of gross)<br>",
        monthyl_deductions, p(monthyl_deductions, monthly_gross_pay))
    prf("Total Taxes: $%.2f<br>", total_taxes)
}

function print_giving(title, giving) {
    prf('<h2>%s</h2>', title)

    giving_total = 0
    giving_total_deductible = 0
    for(var beneficiary in giving) {
        pr('<h3>' + beneficiary + '</h3>')
        details = giving[beneficiary]

        prf("<p>Tax Deductible: %s</p>", details['tax_deductible'] ?
            '<span style=\"color: green;\">Yes</span>' :
            '<span style=\"color: red;\">No</span>')

        pr('<table>')
        pr('<tr> <th>#</th> <th>Date</th> <th>Amount</th> <th>Notes</th> </tr>')
        i = 0
        beneficiary_total = 0
        for(var i in details['transfers']) {
            transfer = details['transfers'][i]
            transfer_date = moment(transfer['date'])
            transfer_amt = transfer['amount']

            pr("<tr>")
            pr("<td>" + ++i + "</td>")
            pr("<td>" + transfer_date.format("ddd MMM Do, YYYY") + "</td>")
            prf("<td>$%.2f</td>", transfer_amt)

            if (transfer.hasOwnProperty('desc')) {
                prf("<td>%s</td>", transfer['desc'])
            };

            beneficiary_total += transfer_amt
        }
        prf('<tr> <th></th> <th>Total</th> <th>$%.2f</th> <th></th> </tr>', beneficiary_total)
        pr('</table>')

        giving_total += beneficiary_total
        if (details['tax_deductible']) {
            giving_total_deductible += beneficiary_total
        }
    }
    prmf("Total Giving: $%.2f", giving_total)
    prmf("Deductible Giving: $%.2f", giving_total_deductible)
}

function print_commitments(giving) {
    pr("<ol>")
    total = 0
    for(i in giving) {
        g = giving[i]

        pr("<li>")
        pru(g['name'])
        if (g.hasOwnProperty('desc')) {
            pr('<p>' + g['desc'] + '</p>')
        }
        if (g.hasOwnProperty('formula')) {
            prm(g['formula'] + ' = __$' + g['annual_total'] + '__')
        }
        else {
            prm('Total: __$' + g['annual_total'] + '__')
        }
        total += g['annual_total']
        pr("</li>")
    }
    pr("</ol>")
    return total
}
