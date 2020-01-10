<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Payment request form</title>
</head>
<body>
<form name="paymentRequestForm" id="paymentRequestForm" action="https://merchant.webmoney.ru/lmi/payment.asp"
      accept-charset="utf-8" method="POST">
    <input type="hidden" name="LMI_PAYMENT_AMOUNT" value="${price}">
    <input type="hidden" name="LMI_PAYMENT_DESC" value="${paymentDescription}">
    <input type="hidden" name="LMI_PAYEE_PURSE" value="${payeePurse}">
    <input type="hidden" name="user_id" value="${userId}">
    <input type="hidden" name="LMI_ALLOW_SDP" value="${paymentType}"></form>
<script>
    document.paymentRequestForm.submit();
</script>
</body>
</html>