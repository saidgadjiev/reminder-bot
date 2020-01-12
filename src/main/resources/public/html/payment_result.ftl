<div class="container d-flex justify-content-center align-items-center flex-column flex-grow-1">
    <div id="open-link" class="w-100 justify-content-center">
        <div class="row justify-content-center">
            <div class="col-md-5">
                <div class="header text-center">
                    <#if fail>
                        <h4 style="color: #E53935">${paymentResult}</h4>
                    <#else>
                        <h4 style="color: #1565C0">${paymentResult}</h4>
                    </#if>
                </div>
                <p class="mt-5" style="text-align: justify">${redirect}</p>
                <a id="link" class="btn btn-sm btn-primary btn-block" href="tg://resolve?domain=${botName}">Перейти в Reminder</a>
            </div>
        </div>
    </div>
</div>
<script type="application/javascript">
    setTimeout(
        function () {
            window.location.href = 'tg://resolve?domain=${botName}';
        }, 2000
    );
</script>