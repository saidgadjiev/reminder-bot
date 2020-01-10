<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="webmoney" content="A1CA1AE6-78A7-4F20-8E73-6065B27C787C"/>
    <title>Reminder bot</title>
    <style>
        table {
            border-collapse: collapse;
        }

        table, td, th {
            border: 1px solid black;
        }
    </style>
</head>
<body style="background-color: #BBDEFB">
<h1 style="text-align: center">Бот Reminder</h1>
<div style="text-align: center; font-size: 20px">
    Бот для создания напоминаний как себе так и друзьям. <b>С этим ботом вы ничего не забудете</b>. <a href="https://t.me/mega_reminder_bot">Ссылка на бот</a>
</div>
<br>
<div style="text-align: center; font-size: 20px;">
    Бот работает по системе подписок. Пробный период <b>${trialPeriod} дней</b>. Далее оплата по плану:
</div>
<br>
<div>
    <table style="margin-left: auto; margin-right: auto">
        <tr>
            <th>Цена</th>
            <th>Период</th>
        </tr>
        <tr><td>${price} рублей</td><td>${period}</td></tr>
    </table>
</div>
</body>
</html>