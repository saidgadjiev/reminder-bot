<div class="header" style="font-size: 15px">
    ${paymentResult}
</div>
<div class="container d-flex justify-content-center align-items-center flex-column flex-grow-1">
    <div id="open-link" class="w-100 justify-content-center">
        <div class="row justify-content-center">
            <div class="col-md-5">
                ${redirect}
                <a id="link" class="btn btn-lg btn-primary btn-block" href="tg://resolve?domain=${botName}">Открыть</a>
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