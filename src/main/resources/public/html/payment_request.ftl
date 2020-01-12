<form name="paymentRequestForm" id="paymentRequestForm" action="https://merchant.webmoney.ru/lmi/payment.asp"
      accept-charset="utf-8" method="POST">
    <input type="hidden" name="LMI_PAYMENT_AMOUNT" value="${price}">
    <input type="hidden" name="LMI_PAYMENT_DESC_BASE64" value="${paymentDescription}">
    <input type="hidden" name="LMI_PAYEE_PURSE" value="${payeePurse}">
    <input type="hidden" name="user_id" value="${userId?c}">
    <input type="hidden" name="plan_id" value="${planId?c}">
    <input type="hidden" name="LMI_ALLOW_SDP" value="${paymentType}"></form>
<script>
    document.paymentRequestForm.submit();
</script>